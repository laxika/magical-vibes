package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImplementOfFerocity;
import com.github.laxika.magicalvibes.cards.p.PrizefighterConstruct;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetallicRebuke.class, GrizzlyBears.class, Forest.class,
        ImplementOfFerocity.class, PrizefighterConstruct.class})
class MetallicRebukeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay {3}")
    void countersWhenOpponentCannotPay() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MetallicRebuke()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Allows a spell to resolve when its controller pays {3}")
    void allowsPayment() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.setHand(player2, List.of(new MetallicRebuke()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new MetallicRebuke()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player2,
                0,
                harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersWhenControllerDeclinesAffordablePayment() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player2, List.of(new MetallicRebuke()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void canImproviseWithNoncreatureArtifactAndSummoningSickArtifactCreature() {
        PrizefighterConstruct target = new PrizefighterConstruct();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        Permanent implement = harness.addToBattlefieldAndReturn(player2, new ImplementOfFerocity());
        Permanent construct = harness.addToBattlefieldAndReturn(player2, new PrizefighterConstruct());
        construct.setSummoningSick(true);
        harness.setHand(player2, List.of(new MetallicRebuke()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithConvoke(player2, 0, List.of(target.getId()),
                List.of(implement.getId(), construct.getId()));

        assertThat(implement.isTapped()).isTrue();
        assertThat(construct.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Prizefighter Construct");
        harness.assertNotOnBattlefield(player1, "Prizefighter Construct");
        harness.assertInGraveyard(player2, "Metallic Rebuke");
    }

    @Test
    void improviseCannotPayTheBlueManaRequirement() {
        PrizefighterConstruct target = new PrizefighterConstruct();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ImplementOfFerocity());
        harness.setHand(player2, List.of(new MetallicRebuke()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player2, 0,
                List.of(target.getId()), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player2, "Metallic Rebuke");
    }

    @Test
    void controllerCanActivateManaAbilitiesWhenPaymentIsRequested() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player2, List.of(new MetallicRebuke()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(bears.getId());
    }
}
