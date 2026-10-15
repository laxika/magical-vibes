package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrowingDread;
import com.github.laxika.magicalvibes.cards.d.DaggermawMegalodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NikoLightOfHope.class, GrizzlyBears.class, GrowingDread.class, DaggermawMegalodon.class})
class NikoLightOfHopeTest extends BaseCardTest {

    @Test
    void enteringCreatesTwoShards() {
        addReadyNiko();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    void abilityCopiesShardsUntilNextEndStepAndReturnsTheCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent niko = addReadyNiko();
        List<Permanent> shards = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        int nikoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(niko);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, nikoIndex, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(shards).allSatisfy(shard -> {
            assertThat(gqs.isCreature(gd, shard)).isTrue();
            assertThat(gqs.getEffectivePower(gd, shard)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, shard)).isEqualTo(2);
        });

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(shards).allSatisfy(shard -> assertThat(gqs.isCreature(gd, shard)).isFalse());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void shardSacrificeScriesBeforeDrawing() {
        addReadyNiko();
        harness.setHand(player1, List.of());
        Card top = new DaggermawMegalodon();
        Card next = new GrowingDread();
        harness.setLibrary(player1, List.of(top, next));
        Permanent shard = findPermanent(player1, "Shard");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shard),
                0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shard);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(next);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void faceDownCreatureIsCopiedUsingItsFaceDownCharacteristics() {
        Permanent niko = addReadyNiko();
        Card manifestedCard = new DaggermawMegalodon();
        harness.setLibrary(player1, List.of(manifestedCard, new GrowingDread()));
        harness.enterBattlefieldAndReturn(player1, new GrowingDread());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown).findFirst().orElseThrow();
        List<Permanent> shards = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(niko),
                0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shards).allSatisfy(shard -> {
            assertThat(gqs.isCreature(gd, shard)).isTrue();
            assertThat(gqs.getEffectivePower(gd, shard)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, shard)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, shard, Keyword.VIGILANCE)).isFalse();
            assertThat(shard.isFaceDown()).isFalse();
        });
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Daggermaw Megalodon");
    }

    @Test
    void onlyControllersShardsBecomeCopies() {
        harness.enterBattlefieldAndReturn(player2, new NikoLightOfHope());
        harness.passBothPriorities();
        List<Permanent> opposingShards = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        Permanent niko = addReadyNiko();
        Permanent creature = addCreatureReady(player1, new DaggermawMegalodon());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(niko),
                0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(opposingShards).hasSize(2)
                .allSatisfy(shard -> assertThat(gqs.isCreature(gd, shard)).isFalse());
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .hasSize(2).allSatisfy(shard -> {
                    assertThat(gqs.getEffectivePower(gd, shard)).isEqualTo(5);
                    assertThat(gqs.hasKeyword(gd, shard, Keyword.VIGILANCE)).isTrue();
                });
    }

    @Test
    void activationDuringEndStepLastsUntilFollowingEndStep() {
        Permanent niko = addReadyNiko();
        Permanent creature = addCreatureReady(player1, new DaggermawMegalodon());
        List<Permanent> shards = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        harness.setLibrary(player2, List.of(new DaggermawMegalodon(), new GrowingDread()));
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(niko),
                0, null, creature.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertNotOnBattlefield(player1, "Daggermaw Megalodon");
        assertThat(shards).allSatisfy(shard -> {
            assertThat(gqs.isCreature(gd, shard)).isTrue();
            assertThat(gqs.getEffectivePower(gd, shard)).isEqualTo(5);
        });

        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(shards).allSatisfy(shard -> assertThat(gqs.isCreature(gd, shard)).isFalse());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Daggermaw Megalodon");
    }

    @Test
    void creatureStillReturnsWhenThereAreNoShards() {
        Permanent niko = addCreatureReady(player1, new NikoLightOfHope());
        Permanent creature = addCreatureReady(player1, new DaggermawMegalodon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(niko),
                0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(niko.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Daggermaw Megalodon");
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Daggermaw Megalodon");
    }

    private Permanent addReadyNiko() {
        Permanent niko = harness.enterBattlefieldAndReturn(player1, new NikoLightOfHope());
        harness.passBothPriorities();
        niko.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return niko;
    }

}
