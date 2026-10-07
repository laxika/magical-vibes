package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.l.LichsMastery;
import com.github.laxika.magicalvibes.cards.s.SwordOfFeastAndFamine;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.s.ShimmerMyr;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TezzeretAgentOfBolas.class, ShimmerMyr.class, LeoninSkyhunter.class, SwordOfFeastAndFamine.class, LichsMastery.class})
class TezzeretAgentOfBolasTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with 3 loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new TezzeretAgentOfBolas()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Tezzeret, Agent of Bolas"));
        Permanent tezz = bf.stream().filter(p -> p.getCard().getName().equals("Tezzeret, Agent of Bolas")).findFirst().orElseThrow();
        assertThat(tezz.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-1 makes target artifact a 5/5 creature")
    void minusOneAnimatesArtifact() {
        Permanent tezz = addReadyTezzeret(player1);
        Permanent solRing = addArtifact(player1);

        harness.activateAbility(player1, 0, 1, null, solRing.getId());
        harness.passBothPriorities();

        assertThat(tezz.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(solRing.isPermanentlyAnimated()).isTrue();
        assertThat(solRing.getEffectivePower()).isEqualTo(5);
        assertThat(solRing.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("-1 animation persists across turns (not until end of turn)")
    void minusOneAnimationPersistsAcrossTurns() {
        addReadyTezzeret(player1);
        Permanent solRing = addArtifact(player1);

        harness.activateAbility(player1, 0, 1, null, solRing.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        // Animation should persist - it's permanent, not "until end of turn"
        assertThat(solRing.isPermanentlyAnimated()).isTrue();
        assertThat(solRing.getEffectivePower()).isEqualTo(5);
        assertThat(solRing.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("-1 cannot target a non-artifact permanent")
    void minusOneCannotTargetNonArtifact() {
        addReadyTezzeret(player1);
        // Add a creature (not an artifact)
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("-1 animated artifact retains artifact type")
    void minusOneAnimatedArtifactRetainsArtifactType() {
        addReadyTezzeret(player1);
        Permanent solRing = addArtifact(player1);

        harness.activateAbility(player1, 0, 1, null, solRing.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().isArtifact(gd, solRing)).isTrue();
        assertThat(harness.getGameQueryService().isCreature(gd, solRing)).isTrue();
    }

    @Test
    @DisplayName("-1 on equipped Equipment unattaches it (CR 301.5c)")
    void minusOneOnEquipmentUnattachesIt() {
        addReadyTezzeret(player1);
        // Add a creature and equip it
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());

        Permanent swordPerm = harness.addToBattlefieldAndReturn(player1, new SwordOfFeastAndFamine());
        swordPerm.setAttachedTo(creature.getId());

        // Animate the equipment
        harness.activateAbility(player1, 0, 1, null, swordPerm.getId());
        harness.passBothPriorities();

        // Equipment should be unattached and animated
        assertThat(swordPerm.getAttachedTo()).isNull();
        assertThat(swordPerm.isPermanentlyAnimated()).isTrue();
        assertThat(swordPerm.getEffectivePower()).isEqualTo(5);
        assertThat(swordPerm.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("-4 drains target player for twice artifact count")
    void minusFourDrainsForTwiceArtifactCount() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setCounterCount(CounterType.LOYALTY, 4);
        addArtifact(player1);
        addArtifact(player1);
        addArtifact(player1);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // 3 artifacts × 2 = 6 life drained
        harness.assertLife(player2, 14);
        harness.assertLife(player1, 26);
        assertThat(tezz.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
    }

    @Test
    @DisplayName("-4 drains zero with no artifacts")
    void minusFourDrainsZeroWithNoArtifacts() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setCounterCount(CounterType.LOYALTY, 4);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // No artifacts = 0 drain
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("-4 does not count opponent's artifacts")
    void minusFourDoesNotCountOpponentArtifacts() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setCounterCount(CounterType.LOYALTY, 4);
        addArtifact(player1);
        addArtifact(player2);
        addArtifact(player2);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // Only 1 artifact controlled by player1 × 2 = 2 life
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("-4 can target self")
    void minusFourCanTargetSelf() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setCounterCount(CounterType.LOYALTY, 4);
        addArtifact(player1);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        // 1 artifact × 2 = 2. Player1 loses 2 then gains 2 = net 0
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot activate -4 with only 3 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyTezzeret(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Tezzeret dies when -4 brings loyalty to 0 but ability still resolves")
    void minusFourKillsTezzeretButAbilityResolves() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setCounterCount(CounterType.LOYALTY, 4);
        addArtifact(player1);
        addArtifact(player1);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // Tezzeret should be dead (0 loyalty)
        harness.assertNotOnBattlefield(player1, "Tezzeret, Agent of Bolas");
        // But ability still resolved: 2 artifacts × 2 = 4
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
    }

    @Test
    void plusOneSelectsOnlyOneArtifactAndOrdersTheRestBelowUnseenCards() {
        Permanent tezz = addReadyTezzeret(player1);
        Card first = new ShimmerMyr();
        Card second = new LeoninSkyhunter();
        Card third = new SwordOfFeastAndFamine();
        Card fourth = new LeoninSkyhunter();
        Card fifth = new LeoninSkyhunter();
        Card unseen = new ShimmerMyr();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, unseen));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tezz.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(first, third);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(first, second, fourth, fifth);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, fifth, fourth, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void plusOneMayDeclineAnArtifactInAShortLibrary() {
        addReadyTezzeret(player1);
        Card artifact = new ShimmerMyr();
        Card creature = new LeoninSkyhunter();
        harness.setLibrary(player1, List.of(artifact, creature));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void plusOneWithoutArtifactsStillAllowsBottomOrdering() {
        addReadyTezzeret(player1);
        Card first = new LeoninSkyhunter();
        Card second = new LeoninSkyhunter();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void plusOneWithEmptyLibraryStillAddsLoyalty() {
        Permanent tezz = addReadyTezzeret(player1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tezz.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void minusOneCanAnimateOpponentsNoncreatureArtifact() {
        addReadyTezzeret(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SwordOfFeastAndFamine());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isPermanentlyAnimated()).isTrue();
        assertThat(artifact.getEffectivePower()).isEqualTo(5);
        assertThat(artifact.getEffectiveToughness()).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Sword of Feast and Famine");
    }

    @Test
    void minusFourCountsArtifactsAtResolution() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setCounterCount(CounterType.LOYALTY, 4);
        addArtifact(player1);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        addArtifact(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
    }

    @Test
    void minusFourTriggersTargetsLifeLossAbilities() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setCounterCount(CounterType.LOYALTY, 4);
        addArtifact(player1);
        harness.addToBattlefield(player2, new LichsMastery());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
        assertThat(gd.lifeLostThisTurn.get(player2.getId())).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lich's Mastery");
    }

    private Permanent addReadyTezzeret(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TezzeretAgentOfBolas());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ShimmerMyr());
    }
}
