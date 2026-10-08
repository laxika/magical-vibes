package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaponizedScrap.class, Ornithopter.class, SoulWarden.class, Unsummon.class})
class WeaponizedScrapTest extends BaseCardTest {

    @Test
    void coversTheOnlyArtifactAndKeepsBothCardsTogether() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.castFromHand(player1, new WeaponizedScrap(), "{4}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(artifact);
        assertThat(artifact.getCard()).isInstanceOf(WeaponizedScrap.class);
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HASTE)).isTrue();
        assertThat(artifact.cardsLeavingBattlefield().stream()
                .map(card -> card.getClass().getSimpleName())
                .toList())
                .containsExactly("WeaponizedScrap", "Ornithopter");
    }

    @Test
    void offersAChoiceAmongMultipleArtifacts() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.castFromHand(player1, new WeaponizedScrap(), "{4}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.getCard()).isInstanceOf(Ornithopter.class);
        assertThat(second.getCard()).isInstanceOf(WeaponizedScrap.class);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void exilesItWhenNoArtifactCanBeCovered() {
        harness.castFromHand(player1, new WeaponizedScrap(), "{4}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(WeaponizedScrap.class::isInstance);
    }

    @Test
    void preservesTappedStateCountersAndDamageButIgnoresCoveredAbilities() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();
        artifact.setMarkedDamage(1);
        artifact.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castFromHand(player1, new WeaponizedScrap(), "{4}");
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getMarkedDamage()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.FLYING)).isFalse();
    }

    @Test
    void cannotCoverAnOpponentsArtifact() {
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        harness.castFromHand(player1, new WeaponizedScrap(), "{4}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingArtifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(WeaponizedScrap.class::isInstance);
    }

    @Test
    void repeatedUpgradesAllReturnToHandTogether() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.castFromHand(player1, new WeaponizedScrap(), "{4}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new WeaponizedScrap(), "{4}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, artifact.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId()).stream().filter(WeaponizedScrap.class::isInstance))
                .hasSize(2);
        harness.assertInHand(player1, "Ornithopter");
    }

    @Test
    void upgradeDoesNotTriggerCreatureEntryAbilities() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new WeaponizedScrap(), "{4}");
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void impossibleUpgradeIsNotAnExileFromTheBattlefield() {
        harness.castFromHand(player1, new WeaponizedScrap(), "{4}");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(WeaponizedScrap.class::isInstance);
        assertThat(gd.creatureExileCountThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void ownerChoosesComponentOrderWhenPutOnTopOfLibrary() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.castFromHand(player1, new WeaponizedScrap(), "{4}");
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToLibraryTop(gd, artifact));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void ownerChoosesComponentOrderWhenPutOnBottomOfLibrary() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.castFromHand(player1, new WeaponizedScrap(), "{4}");
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToLibraryBottom(gd, artifact));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
