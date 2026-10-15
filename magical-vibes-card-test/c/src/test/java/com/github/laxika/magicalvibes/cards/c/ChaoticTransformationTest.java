package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AllIsDust;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartbeatOfSpring;
import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.Meteorite;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.o.OmegaMyr;
import com.github.laxika.magicalvibes.cards.s.SoulSculptor;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.z.ZhurTaaGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        ChaoticTransformation.class,
        AllIsDust.class,
        Bitterblossom.class,
        CombatResearch.class,
        Forest.class,
        FountainOfYouth.class,
        GrizzlyBears.class,
        HeartbeatOfSpring.class,
        MoxOpal.class,
        MarchOfTheMachines.class,
        MindStone.class,
        Meteorite.class,
        NicolBolasPlaneswalker.class,
        OmegaMyr.class,
        PsychogenicProbe.class,
        ZhurTaaGoblin.class,
        SoulSculptor.class
})
class ChaoticTransformationTest extends BaseCardTest {

    @Test
    void riotChoiceFinishesBeforeEntryAndLibraryShuffle() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card skipped = new FountainOfYouth();
        Card revealed = new ZhurTaaGoblin();
        harness.setLibrary(player2, List.of(skipped, revealed));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(skipped, revealed);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() == revealed);
        assertThat(gd.stack).isEmpty();
        harness.handleMayAbilityChosen(player2, true);

        Permanent entered = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == revealed).findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(skipped);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void exilesEachTargetAndReplacesItWithApermanentSharingItsCardType() {
        Card artifactCard = new FountainOfYouth();
        Card creatureCard = new GrizzlyBears();
        Card enchantmentCard = new HeartbeatOfSpring();
        Card planeswalkerCard = new NicolBolasPlaneswalker();
        Card landCard = new Forest();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, artifactCard);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, enchantmentCard);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, planeswalkerCard);
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent land = harness.addToBattlefieldAndReturn(player2, landCard);

        harness.setLibrary(player2, List.of(
                new Forest(), new HeartbeatOfSpring(),
                new NicolBolasPlaneswalker(), new MoxOpal(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0,
                List.of(artifact.getId(), creature.getId(), enchantment.getId(), planeswalker.getId(), land.getId()));
        finishReplacementOrderChoices();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(artifactCard, creatureCard, enchantmentCard, planeswalkerCard, landCard);
        assertThat(countPermanents(player2, "Mox Opal")).isEqualTo(1);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player2, "Heartbeat of Spring")).isEqualTo(1);
        assertThat(countPermanents(player2, "Nicol Bolas, Planeswalker")).isEqualTo(1);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
    }

    @Test
    void allowsAnArtifactCreatureToFillBothCategoriesButReplacesItOnce() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new OmegaMyr());
        Card creatureLeftInLibrary = new GrizzlyBears();
        harness.setLibrary(player2, List.of(new MoxOpal(), creatureLeftInLibrary));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0, List.of(artifactCreature.getId(), artifactCreature.getId()));

        harness.assertNotOnBattlefield(player2, "Omega Myr");
        assertThat(countPermanents(player2, "Mox Opal")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creatureLeftInLibrary);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(artifactCreature.getCard());
    }

    @Test
    void rejectsMoreThanOneTargetForTheSameCardType() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("declared target groups");
    }

    @Test
    void mayBeCastWithNoTargets() {
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void snapshotsAllTargetTypesBeforeAnyTargetLeavesTheBattlefield() {
        Permanent animation = harness.addToBattlefieldAndReturn(player2, new MarchOfTheMachines());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Card replacementCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(new HeartbeatOfSpring(), replacementCreature));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0, List.of(animation.getId(), artifact.getId()));
        finishReplacementOrderChoices();

        harness.assertOnBattlefield(player2, "Heartbeat of Spring");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(animation.getCard(), artifact.getCard());
    }

    @Test
    void controllerChoosesReplacementOrderForMultipleExiledPermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new OmegaMyr(), new GrizzlyBears(), new MoxOpal()));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0, List.of(artifact.getId(), creature.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(artifact.getCard(), creature.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        PendingInteraction.ColorChoice order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order.context()).isInstanceOf(ChoiceContext.PermanentReplacementOrder.class);
        harness.handleListChoice(player2, order.options().get(1));
        harness.assertOnBattlefield(player2, "Omega Myr");
        harness.assertOnBattlefield(player2, "Mox Opal");
        assertThat(gd.playerDecks.get(player2.getId()))
                .singleElement().satisfies(card -> assertThat(card.getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    void stopsAtSharedKindredTypeEvenWhenTheRevealedCardCannotEnterTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Bitterblossom());
        Card kindredSorcery = new AllIsDust();
        Card enchantment = new HeartbeatOfSpring();
        harness.setLibrary(player2, List.of(kindredSorcery, enchantment));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(kindredSorcery, enchantment);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    void revealedAuraEntersAttachedToAChosenLegalCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HeartbeatOfSpring());
        Permanent host = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new CombatResearch()));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, host.getId());
        harness.assertOnBattlefield(player2, "Combat Research");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Combat Research"))
                .singleElement().satisfies(p -> assertThat(p.getAttachedTo()).isEqualTo(host.getId()));
    }

    @Test
    void shufflesAllRevealedCardsBackWhenThereIsNoMatchingCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card land = new Forest();
        Card sorcery = new ChaoticTransformation();
        harness.setLibrary(player2, List.of(land, sorcery));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(land, sorcery);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    void exilesTheTargetEvenWhenItsControllersLibraryIsEmpty() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    void eachControllerUsesTheirOwnLibrary() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new MoxOpal()));
        harness.setLibrary(player2, List.of(new OmegaMyr()));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0, List.of(artifact.getId(), creature.getId()));

        harness.assertOnBattlefield(player1, "Mox Opal");
        harness.assertOnBattlefield(player2, "Omega Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(artifact.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void creatureTargetBecomesIllegalWhenItBecomesOnlyAnEnchantment() {
        addCreatureReady(player2, new SoulSculptor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card libraryCard = new HeartbeatOfSpring();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castSorcery(player1, 0, List.of(target.getId()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Chaotic Transformation");
    }

    @Test
    void revealedNoncreatureArtifactTriggersItsEnterAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of(new Meteorite()));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        harness.assertOnBattlefield(player2, "Meteorite");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    private void finishReplacementOrderChoices() {
        PendingInteraction.ColorChoice order;
        while ((order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)) != null
                && order.context() instanceof ChoiceContext.PermanentReplacementOrder) {
            harness.handleListChoice(player2, order.options().getFirst());
        }
    }

    private void addManaForChaoticTransformation() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
