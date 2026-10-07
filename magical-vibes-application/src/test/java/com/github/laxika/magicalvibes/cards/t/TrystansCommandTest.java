package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrystansCommand.class, LlanowarElves.class, GrizzlyBears.class, Spellbook.class, GloriousAnthem.class})
class TrystansCommandTest extends BaseCardTest {

    @Test
    void copyAndDestroyModesResolveInCardTextOrder() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(elf.getId(), elf.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    void graveyardReturnModePreservesOtherModeTarget() {
        Card creatureCard = new GrizzlyBears();
        Card artifactCard = new Spellbook();
        harness.setGraveyard(player1, List.of(creatureCard, artifactCard));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2},
                List.of(creature.getId()));

        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId(), artifactCard.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Spellbook");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void boostAndUntapModeAffectsOnlyTargetPlayersCreatures() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        targetCreature.tap();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.tap();
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3},
                List.of(elf.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(5);
        assertThat(targetCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(ownCreature.isTapped()).isTrue();
    }

    @Test
    void copyModeRejectsElfControlledByOpponent() {
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(opponentElf.getId(), opponentElf.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnModeRequiresAtLeastOnePermanentCardInGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2,
                new int[]{1, 2}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnModeRejectsChoosingZeroCards() {
        harness.setGraveyard(player1, List.of(new Spellbook()));
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3},
                List.of(player1.getId()));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnModeAllowsOneCardAndPreservesPlayerTarget() {
        Card artifact = new Spellbook();
        Card sorcery = new TrystansCommand();
        harness.setGraveyard(player1, List.of(artifact, sorcery));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3},
                List.of(player2.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spellbook");
        harness.assertNotInHand(player1, "Trystan's Command");
        harness.assertInGraveyard(player1, "Trystan's Command");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void returnModeRejectsNonPermanentCard() {
        Card artifact = new Spellbook();
        Card sorcery = new TrystansCommand();
        harness.setGraveyard(player1, List.of(artifact, sorcery));
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3},
                List.of(player1.getId()));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(sorcery.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroyModeAcceptsNonCreatureEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3},
                List.of(enchantment.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void destroyModeRejectsNonCreatureNonEnchantmentArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2,
                new int[]{2, 3}, List.of(artifact.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyModeRejectsNonElfYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2,
                new int[]{0, 3}, List.of(creature.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copiedElfReceivesBoostBecauseCopyModeResolvesFirst() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elf.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        artifact.tap();
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3},
                List.of(elf.getId(), player1.getId()));
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(4);
        assertThat(tokens.getFirst().isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(elf.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isTrue();

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(1);
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
