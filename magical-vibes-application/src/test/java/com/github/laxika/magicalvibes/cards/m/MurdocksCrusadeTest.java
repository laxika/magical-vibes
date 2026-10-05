package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.d.DemonicPact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MurdocksCrusade.class, CrawWurm.class, DemonicPact.class, GrizzlyBears.class, Solemnity.class})
class MurdocksCrusadeTest extends BaseCardTest {

    @Test
    void streetJusticeExilesCreatureWithToughnessAtLeastFour() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        cast(new int[]{0}, List.of(creature.getId()), List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName).contains("Craw Wurm");
    }

    @Test
    void legalJusticeExilesEnchantmentWithManaValueAtLeastFour() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DemonicPact());

        cast(new int[]{1}, List.of(enchantment.getId()), List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName).contains("Demonic Pact");
    }

    @Test
    void teamworkAllowsBothModesAndTapsTheChosenCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DemonicPact());
        Permanent teammate = addCreatureReady(player1, new CrawWurm());

        cast(new int[]{0, 1}, List.of(creature.getId(), enchantment.getId()), List.of(teammate.getId()));

        assertThat(teammate.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Craw Wurm", "Demonic Pact");
    }

    @Test
    void teamworkCannotChooseOnlyOneMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new CrawWurm());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(creature.getId()), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires choosing all modes");
        assertThat(teammate.isTapped()).isFalse();
    }

    @Test
    void modesRejectTargetsThatDoNotMeetTheirRestrictions() {
        Permanent smallCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent smallEnchantment = harness.addToBattlefieldAndReturn(player2, new Solemnity());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(smallCreature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> cast(new int[]{1}, List.of(smallEnchantment.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesCannotBeChosenWithoutTeamwork() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DemonicPact());

        assertThatThrownBy(() -> cast(new int[]{0, 1},
                List.of(creature.getId(), enchantment.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void teamworkAcceptsSummoningSickCreaturesWithExactlyFourTotalPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DemonicPact());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setSummoningSick(true);
        second.setSummoningSick(true);

        cast(new int[]{0, 1}, List.of(creature.getId(), enchantment.getId()),
                List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Craw Wurm", "Demonic Pact");
    }

    @Test
    void teamworkRejectsInsufficientTotalPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DemonicPact());
        Permanent teammate = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{0, 1},
                List.of(creature.getId(), enchantment.getId()), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(teammate.isTapped()).isFalse();
    }

    @Test
    void teamworkRejectsAlreadyTappedCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DemonicPact());
        Permanent teammate = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        teammate.setTapped(true);

        assertThatThrownBy(() -> cast(new int[]{0, 1},
                List.of(creature.getId(), enchantment.getId()), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void teamworkRejectsAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DemonicPact());

        assertThatThrownBy(() -> cast(new int[]{0, 1},
                List.of(creature.getId(), enchantment.getId()), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }
    @Test
    void remainingLegalModeResolvesWhenTheCreatureTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DemonicPact());
        Permanent teammate = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        harness.setHand(player1, List.of(new MurdocksCrusade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 2, new int[]{0, 1},
                List.of(creature.getId(), enchantment.getId()), List.of(teammate.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .containsExactly("Demonic Pact");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        assertThat(teammate.isTapped()).isTrue();
    }
    private void cast(int[] modes, List<java.util.UUID> targetIds, List<java.util.UUID> teamworkIds) {
        harness.setHand(player1, List.of(new MurdocksCrusade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalSorceryWithModesAndTaps(
                player1, 0, 1, 2, modes, targetIds, teamworkIds);
        harness.passBothPriorities();
    }
}
