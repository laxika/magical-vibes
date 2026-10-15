package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.o.OjerTaqDeepestFoundation;
import com.github.laxika.magicalvibes.cards.t.TempleOfCivilization;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LazotepArchway.class, OjerTaqDeepestFoundation.class, TempleOfCivilization.class})
class LazotepArchwayTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new LazotepArchway()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Lazotep Archway").isTapped()).isTrue();
    }

    @Test
    void addsOneManaOfTheChosenColor() {
        harness.addToBattlefield(player1, new LazotepArchway());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void eternalizeCreatesTappedCreatureLandZombieToken() {
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player1, List.of(new LazotepArchway()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Lazotep Archway");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Lazotep Archway"));

        Permanent token = findPermanent(player1, "Lazotep Archway");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(token.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    void addsBlackManaAndPaysTapCostWithoutUsingTheStack() {
        harness.addToBattlefield(player1, new LazotepArchway());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(findPermanent(player1, "Lazotep Archway").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eternalizeExilesTheCardBeforeResolving() {
        prepareEternalize();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Lazotep Archway");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Lazotep Archway"));
        harness.assertNotOnBattlefield(player1, "Lazotep Archway");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void eternalizeCannotBeActivatedDuringCombat() {
        prepareEternalize();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Lazotep Archway");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eternalizeCannotBeActivatedOnAnOpponentsTurn() {
        prepareEternalize();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Lazotep Archway");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eternalizeCannotBeActivatedWithAnotherAbilityOnTheStack() {
        prepareEternalize();
        harness.setGraveyard(player1, List.of(new LazotepArchway(), new LazotepArchway()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void eternalizedTokenRetainsItsManaAbilityAfterUntapping() {
        prepareEternalize();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Lazotep Archway");
        token.untap();
        token.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(token.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({OjerTaqDeepestFoundation.class, TempleOfCivilization.class})
    void eternalizeCreatesThreeCreatureTokensWithOjerTaq() {
        prepareEternalize();
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lazotep Archway")).hasSize(3)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(token.isTapped()).isTrue();
                });
    }

    private void prepareEternalize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LazotepArchway()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
