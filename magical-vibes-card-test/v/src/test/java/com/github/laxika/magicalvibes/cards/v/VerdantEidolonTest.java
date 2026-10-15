package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.s.SimicInitiate;
import com.github.laxika.magicalvibes.cards.w.WreckingBall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerdantEidolon.class, AzoriusFirstWing.class, SimicInitiate.class, WreckingBall.class})
class VerdantEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Verdant Eidolon adds three mana of one chosen color")
    void sacrificeAbilityAddsThreeManaOfChosenColor() {
        harness.addToBattlefield(player1, new VerdantEidolon());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertInGraveyard(player1, "Verdant Eidolon");
    }

    @Test
    @DisplayName("Casting a multicolored spell may return Verdant Eidolon from the graveyard")
    void multicoloredSpellReturnsEidolonToHand() {
        VerdantEidolon eidolon = new VerdantEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new AzoriusFirstWing(), "{W}{U}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(eidolon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(eidolon);
    }

    @Test
    @DisplayName("Declining the multicolored-spell trigger leaves Verdant Eidolon in the graveyard")
    void decliningReturnLeavesEidolonInGraveyard() {
        VerdantEidolon eidolon = new VerdantEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new AzoriusFirstWing(), "{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eidolon);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("A monocolored spell does not trigger Verdant Eidolon's graveyard ability")
    void monocoloredSpellDoesNotTriggerReturn() {
        VerdantEidolon eidolon = new VerdantEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new SimicInitiate(), "{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("A multicolored spell cast by an opponent does not trigger Verdant Eidolon")
    void opponentsMulticoloredSpellDoesNotTriggerReturn() {
        VerdantEidolon eidolon = new VerdantEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.addToBattlefield(player1, new SimicInitiate());
        harness.setHand(player2, List.of(new WreckingBall()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Simic Initiate"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("Verdant Eidolon on the battlefield does not trigger when a multicolored spell is cast")
    void battlefieldEidolonDoesNotTriggerReturn() {
        harness.addToBattlefield(player1, new VerdantEidolon());
        harness.castFromHand(player1, new AzoriusFirstWing(), "{W}{U}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Verdant Eidolon");
        harness.assertNotInHand(player1, "Verdant Eidolon");
    }

    @Test
    @DisplayName("A sacrificed Verdant Eidolon can return when its controller casts a multicolored spell")
    void sacrificedEidolonReturnsOnLaterMulticoloredCast() {
        VerdantEidolon eidolon = new VerdantEidolon();
        harness.addToBattlefield(player1, eidolon);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Verdant Eidolon");
        harness.assertInGraveyard(player1, "Verdant Eidolon");
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();

        harness.castFromHand(player1, new AzoriusFirstWing(), "{W}{U}");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(eidolon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(eidolon);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each Verdant Eidolon in the graveyard has an independently optional return trigger")
    void multipleEidolonsHaveIndependentReturnChoices() {
        VerdantEidolon first = new VerdantEidolon();
        VerdantEidolon second = new VerdantEidolon();
        harness.setGraveyard(player1, List.of(first, second));
        harness.castFromHand(player1, new AzoriusFirstWing(), "{W}{U}");

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isIn(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst()).isIn(first, second);
        assertThat(gd.playerHands.get(player1.getId()).getFirst())
                .isNotSameAs(gd.playerGraveyards.get(player1.getId()).getFirst());
        assertThat(gd.stack).hasSize(1);
    }
}
