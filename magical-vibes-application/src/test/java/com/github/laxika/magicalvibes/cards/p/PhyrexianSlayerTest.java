package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GlimmeringAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianSlayer.class, GlimmeringAngel.class, PhyrexianBattleflies.class})
class PhyrexianSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked by a white creature destroys that creature")
    void destroysWhiteBlocker() {
        Permanent slayer = addReadySlayer();
        Permanent blocker = addCreatureReady(player2, new GlimmeringAngel());
        blocker.setRegenerationShield(1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(slayer)));
        block(blocker);

        harness.assertNotOnBattlefield(player2, "Glimmering Angel");
        harness.assertInGraveyard(player2, "Glimmering Angel");
    }

    @Test
    @DisplayName("Becoming blocked by a nonwhite creature does not destroy it")
    void doesNotDestroyNonwhiteBlocker() {
        Permanent slayer = addReadySlayer();
        Permanent blocker = addCreatureReady(player2, new PhyrexianBattleflies());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(slayer)));
        block(blocker);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Phyrexian Battleflies"));
    }

    @Test
    @DisplayName("With multiple blockers, only white blockers are destroyed")
    void onlyWhiteBlockersAreDestroyed() {
        Permanent slayer = addReadySlayer();
        Permanent whiteBlocker = addCreatureReady(player2, new GlimmeringAngel());
        Permanent nonwhiteBlocker = addCreatureReady(player2, new PhyrexianBattleflies());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(slayer)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(whiteBlocker.getId()))
                .anyMatch(permanent -> permanent.getId().equals(nonwhiteBlocker.getId()));
    }

    @Test
    @DisplayName("Each white blocker is destroyed when multiple white creatures block")
    void destroysEachWhiteBlocker() {
        Permanent slayer = addReadySlayer();
        Permanent firstWhiteBlocker = addCreatureReady(player2, new GlimmeringAngel());
        Permanent secondWhiteBlocker = addCreatureReady(player2, new GlimmeringAngel());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(slayer)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstWhiteBlocker.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondWhiteBlocker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Glimmering Angel"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Shroud does not protect a white blocker from the nontargeting trigger")
    void destroysWhiteBlockerWithShroud() {
        Permanent slayer = addReadySlayer();
        Permanent blocker = addCreatureReady(player2, new GlimmeringAngel());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(slayer)));
        block(blocker);

        harness.assertNotOnBattlefield(player2, "Glimmering Angel");
        harness.assertInGraveyard(player2, "Glimmering Angel");
    }

    @Test
    @DisplayName("A surviving blocker can regenerate a later destruction in the same turn")
    void regenerationRestrictionAppliesOnlyToSlayerDestruction() {
        Permanent slayer = addReadySlayer();
        Permanent blocker = addCreatureReady(player2, new GlimmeringAngel());
        blocker.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(slayer)));
        block(blocker);
        harness.assertOnBattlefield(player2, "Glimmering Angel");

        blocker.getGrantedKeywords().remove(Keyword.INDESTRUCTIBLE);
        blocker.setRegenerationShield(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .tryDestroyPermanent(gd, blocker));

        harness.assertOnBattlefield(player2, "Glimmering Angel");
        harness.assertNotInGraveyard(player2, "Glimmering Angel");
        assertThat(blocker.getRegenerationShield()).isZero();
    }

    private Permanent addReadySlayer() {
        return addCreatureReady(player1, new PhyrexianSlayer());
    }

    private void block(Permanent blocker) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));
        resolveAllTriggers();
    }
}
