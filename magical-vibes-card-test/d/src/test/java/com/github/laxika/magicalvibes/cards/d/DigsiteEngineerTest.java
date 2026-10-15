package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DigsiteEngineer.class, Spellbook.class, GrizzlyBears.class})
class DigsiteEngineerTest extends BaseCardTest {

    @Test
    void artifactSpellPromptsAndCreatesConstructAfterPaying() {
        harness.addToBattlefield(player1, new DigsiteEngineer());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(construct.getCard().isToken()).isTrue();
        assertThat(construct.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void constructScalesWithArtifactsYouControl() {
        harness.addToBattlefield(player1, new DigsiteEngineer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);
    }

    @Test
    void decliningPaymentDoesNotCreateConstruct() {
        harness.addToBattlefield(player1, new DigsiteEngineer());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Construct")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void nonartifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DigsiteEngineer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Construct")).isEmpty();
    }

    @Test
    void paymentIsNotRequestedBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new DigsiteEngineer());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(findPermanents(player1, "Construct")).isEmpty();
    }

    @Test
    void constructGrowsWhenTriggeringArtifactResolvesAndIgnoresOpponentsArtifacts() {
        harness.addToBattlefield(player1, new DigsiteEngineer());
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Spellbook");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);
    }

    @Test
    void opponentsArtifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DigsiteEngineer());
        harness.setHand(player2, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Construct")).isEmpty();
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    @Test
    void artifactEnteringWithoutBeingCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new DigsiteEngineer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.enterBattlefieldAndReturn(player1, new Spellbook());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Construct")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
