package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CorruptedConviction;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben;
import com.github.laxika.magicalvibes.cards.w.WrennsResolve;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaloForager.class, LightningBolt.class, Murder.class, Shock.class,
        CorruptedConviction.class, WrennsResolve.class, ThaliaGuardianOfThraben.class})
class HaloForagerTest extends BaseCardTest {

    @Test
    void paysXThenCastsMatchingSpellFromOwnGraveyardForFree() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).contains("Shock");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canTargetAnInstantInAnOpponentsGraveyard() {
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player2, List.of(bolt));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertNotInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).contains("Lightning Bolt");
    }

    @Test
    void choosesAmongMatchingCardsAfterPayingX() {
        Shock shock = new Shock();
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(shock));
        harness.setGraveyard(player2, List.of(bolt));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).contains("Lightning Bolt");
    }

    @Test
    void decliningTheCastLeavesTheTargetInItsGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).doesNotContain("Shock");
    }

    @Test
    void noMatchingManaValueCreatesNoReflexiveTrigger() {
        harness.setGraveyard(player1, List.of(new Murder()));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Murder");
    }

    @Test
    void acceptingSpellWithMandatorySacrificeRequiresChoosingTheSacrifice() {
        harness.setGraveyard(player1, List.of(new CorruptedConviction()));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Halo Forager"));
        harness.assertNotOnBattlefield(player1, "Halo Forager");
        harness.assertInGraveyard(player1, "Halo Forager");
    }

    @Test
    void canCastSorceryDuringReflexiveAbilityResolution() {
        WrennsResolve spell = new WrennsResolve();
        HaloForager first = new HaloForager();
        HaloForager second = new HaloForager();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of(spell));
        castHaloForager();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Wrenn's Resolve");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .contains(first.getId(), second.getId(), spell.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void targetLeavingGraveyardBeforeReflexiveTriggerResolvesCannotBeCast() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(shock));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(shock.getId());
    }

    @Test
    void matchingManaValueCreatureIsNotAValidTarget() {
        harness.setGraveyard(player1, List.of(new HaloForager()));
        castHaloForager();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.handleXValueChosen(player1, 3);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Halo Forager");
    }

    @Test
    void payingZeroDoesNotSpendManaOrTargetNonzeroManaValueCard() {
        harness.setGraveyard(player1, List.of(new Shock()));
        castHaloForager();

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void freeSorceryCannotBeCastWhenSpellTaxCannotBePaid() {
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
        harness.setGraveyard(player1, List.of(new WrennsResolve()));
        castHaloForager();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wrenn's Resolve");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .doesNotContain("Wrenn's Resolve");
    }

    private void castHaloForager() {
        harness.castFromHand(player1, new HaloForager(), "{1}{U}{B}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
