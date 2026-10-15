package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OminousRoost.class, Firebolt.class, GrizzlyBears.class, AirElemental.class})
class OminousRoostTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 blue flying Bird token")
    void enteringBattlefieldCreatesBird() {
        castRoost();

        Permanent bird = findPermanent(player1, "Bird");
        assertThat(bird.getEffectivePower()).isEqualTo(1);
        assertThat(bird.getEffectiveToughness()).isEqualTo(1);
        assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting a spell from your graveyard creates a Bird token")
    void castingFromGraveyardCreatesBird() {
        harness.addToBattlefield(player1, new OminousRoost());
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell from hand does not create a Bird token")
    void castingFromHandDoesNotCreateBird() {
        harness.addToBattlefield(player1, new OminousRoost());
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isZero();
    }

    @Test
    @DisplayName("A Bird token cannot block a creature without flying")
    void birdTokenCannotBlockGroundCreature() {
        Permanent bird = createBird();

        Permanent groundAttacker = addAttacker(new GrizzlyBears());
        prepareBlockers();
        final int birdIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bird);
        final int groundAttackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(groundAttacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(birdIndex, groundAttackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Bird token can block a creature with flying")
    void birdTokenCanBlockFlyingCreature() {
        Permanent bird = createBird();
        Permanent flyingAttacker = addAttacker(new AirElemental());
        prepareBlockers();
        int birdIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bird);
        int flyingAttackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(flyingAttacker);

        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(birdIndex, flyingAttackerIndex)));

        assertThat(bird.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An opponent casting from their graveyard does not create a Bird")
    void opponentsGraveyardCastDoesNotCreateBird() {
        harness.addToBattlefield(player1, new OminousRoost());
        harness.forceActivePlayer(player2);
        harness.setGraveyard(player2, List.of(new Firebolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castFlashback(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Bird")).isZero();
        assertThat(countPermanents(player2, "Bird")).isZero();
    }

    @Test
    @DisplayName("Each Roost creates a Bird before the graveyard spell resolves")
    void multipleRoostsTriggerBeforeSpellResolves() {
        harness.addToBattlefield(player1, new OminousRoost());
        harness.addToBattlefield(player1, new OminousRoost());
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);

        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    private void castRoost() {
        harness.setHand(player1, List.of(new OminousRoost()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent createBird() {
        castRoost();
        return findPermanents(player1, "Bird").getLast();
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player2, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private void prepareBlockers() {
        prepareDeclareBlockers(player2);
    }
}
