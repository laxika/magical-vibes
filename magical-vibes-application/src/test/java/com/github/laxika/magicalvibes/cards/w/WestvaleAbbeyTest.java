package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WestvaleAbbey.class, DevilthornFox.class})
class WestvaleAbbeyTest extends BaseCardTest {

    private static final int TOKEN_ABILITY = 1;
    private static final int TRANSFORM_ABILITY = 2;

    @Test
    void manaAbilityResolvesImmediatelyEvenWhenLandIsSummoningSick() {
        Permanent abbey = harness.addToBattlefieldAndReturn(player1, new WestvaleAbbey());
        abbey.setSummoningSick(true);

        harness.activateAbility(player1, indexOf(abbey), 0, null, null);

        assertThat(abbey.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenCostsArePaidBeforeTokenIsCreated() {
        Permanent abbey = addAbbey(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(abbey), TOKEN_ABILITY, null, null);

        assertThat(abbey.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(countPermanents(player1, "Human Cleric")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human Cleric");
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.CLERIC);
        assertThat(token.isTapped()).isFalse();
        assertThat(countPermanents(player2, "Human Cleric")).isZero();
    }

    @Test
    void tokenAbilityRequiresFiveMana() {
        Permanent abbey = addAbbey(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(abbey), TOKEN_ABILITY, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(abbey.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCreaturesCannotPayTransformCost() {
        Permanent abbey = addAbbey(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new DevilthornFox());
        }
        harness.addToBattlefield(player2, new DevilthornFox());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(abbey), TRANSFORM_ABILITY, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Devilthorn Fox")).isEqualTo(4);
        assertThat(countPermanents(player2, "Devilthorn Fox")).isEqualTo(1);
        assertThat(abbey.isTransformed()).isFalse();
    }

    @Test
    void choosingAmongSixCreaturesSacrificesExactlyFiveBeforeResolution() {
        Permanent abbey = addAbbey(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new DevilthornFox());
        }
        List<Permanent> foxes = findPermanents(player1, "Devilthorn Fox");

        harness.activateAbility(player1, indexOf(abbey), TRANSFORM_ABILITY, null, null);
        harness.handlePermanentChosen(player1, foxes.getFirst().getId());

        assertThat(countPermanents(player1, "Devilthorn Fox")).isEqualTo(5);
        for (int i = 1; i < 5; i++) {
            harness.handlePermanentChosen(player1, foxes.get(i).getId());
        }
        assertThat(countPermanents(player1, "Devilthorn Fox")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(abbey.isTransformed()).isFalse();
        assertThat(abbey.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(abbey.isTransformed()).isTrue();
        assertThat(abbey.isTapped()).isFalse();
    }

    @Test
    void transformedAbbeyHasHasteFlyingLifelinkAndSurvivesLethalDamage() {
        Permanent abbey = harness.addToBattlefieldAndReturn(player1, new WestvaleAbbey());
        abbey.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new DevilthornFox());
        }
        Permanent blocker = addCreatureReady(player2, new DevilthornFox());

        harness.activateAbility(player1, indexOf(abbey), TRANSFORM_ABILITY, null, null);
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, abbey, player1.getId())).isTrue();
        assertThat(bls.canBlockAttacker(gd, blocker, abbey, gd.playerBattlefields.get(player2.getId()))).isFalse();
        declareAttackers(List.of(indexOf(abbey)));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL - 9);
        assertThat(gd.getLife(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL + 9);
        abbey.setMarkedDamage(7);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(abbey);
    }

    @Test
    @DisplayName("Token ability pays 1 life and creates a 1/1 Human Cleric")
    void tokenAbilityCreatesHumanCleric() {
        Permanent abbey = addAbbey(player1);
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(abbey), TOKEN_ABILITY, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL - 1);
        Permanent token = findPermanent(player1, "Human Cleric");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Transform ability sacrifices five creatures, transforms and untaps the land")
    void transformSacrificesFiveCreatures() {
        Permanent abbey = addAbbey(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new DevilthornFox());
        }

        harness.activateAbility(player1, indexOf(abbey), TRANSFORM_ABILITY, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Devilthorn Fox")).hasSize(5);
        assertThat(abbey.isTransformed()).isTrue();
        assertThat(abbey.getCard().getName()).isEqualTo("Ormendahl, Profane Prince");
        assertThat(abbey.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Transform ability cannot be activated with only four creatures")
    void transformNeedsFiveCreatures() {
        Permanent abbey = addAbbey(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new DevilthornFox());
        }

        int index = indexOf(abbey);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, TRANSFORM_ABILITY, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(abbey.isTransformed()).isFalse();
    }

    private Permanent addAbbey(Player player) {
        return addCreatureReady(player, new WestvaleAbbey());
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
