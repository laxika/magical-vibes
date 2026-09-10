package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DuctCrawler.class)
class DuctCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts it on the stack with the chosen creature target")
    void activatingAbilityPutsOnStack() {
        addReadyCrawler(player1);
        Permanent target = addCreatureReady(player2, new DuctCrawler());
        addAbilityMana(player1, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent target = addCreatureReady(player2, new DuctCrawler());
        addAbilityMana(player1, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(crawler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyCrawler(player1);
        Permanent target = addCreatureReady(player2, new DuctCrawler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability with only 1 mana instead of {1}{R}")
    void cannotActivateWithInsufficientMana() {
        addReadyCrawler(player1);
        Permanent target = addCreatureReady(player2, new DuctCrawler());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Resolving ability adds the source object to the target's cant-block restrictions")
    void resolvingAbilityAddsCantBlockRestriction() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent target = addCreatureReady(player2, new DuctCrawler());
        addAbilityMana(player1, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCantBlockIds()).contains(crawler.getId());
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent target = addCreatureReady(player2, new DuctCrawler());
        addAbilityMana(player1, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCantBlockIds()).doesNotContain(crawler.getId());
    }

    @Test
    @DisplayName("Restriction stays tied to the activated source object")
    void restrictionDoesNotFollowNewSourceObject() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent blocker = addCreatureReady(player2, new DuctCrawler());
        addAbilityMana(player1, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(crawler);
        gd.playerGraveyards.get(player1.getId()).add(crawler.getCard());
        harness.passBothPriorities();

        Permanent replacement = addReadyCrawler(player1);
        replacement.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getCantBlockIds())
                .contains(crawler.getId())
                .doesNotContain(replacement.getId());
    }

    @Test
    @DisplayName("Targeted creature cannot block Duct Crawler after ability resolves")
    void targetedCreatureCannotBlockDuctCrawler() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent blocker = addCreatureReady(player2, new DuctCrawler());
        addAbilityMana(player1, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        crawler.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Targeted creature can still block other creatures")
    void targetedCreatureCanBlockOtherCreatures() {
        addReadyCrawler(player1);
        Permanent otherAttacker = addCreatureReady(player1, new DuctCrawler());
        Permanent blocker = addCreatureReady(player2, new DuctCrawler());
        addAbilityMana(player1, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        otherAttacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    @Test
    @DisplayName("Non-targeted creature can still block Duct Crawler")
    void nonTargetedCreatureCanBlockDuctCrawler() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent targetedBlocker = addCreatureReady(player2, new DuctCrawler());
        Permanent otherBlocker = addCreatureReady(player2, new DuctCrawler());
        addAbilityMana(player1, 1);

        harness.activateAbility(player1, 0, null, targetedBlocker.getId());
        harness.passBothPriorities();

        crawler.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
    }

    @Test
    @DisplayName("Can activate ability multiple times on different creatures")
    void canActivateMultipleTimes() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent blocker1 = addCreatureReady(player2, new DuctCrawler());
        Permanent blocker2 = addCreatureReady(player2, new DuctCrawler());
        addAbilityMana(player1, 2);

        harness.activateAbility(player1, 0, null, blocker1.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, blocker2.getId());
        harness.passBothPriorities();

        assertThat(blocker1.getCantBlockIds()).contains(crawler.getId());
        assertThat(blocker2.getCantBlockIds()).contains(crawler.getId());

        crawler.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Blocking restriction resets at end of turn")
    void restrictionResetsAtEndOfTurn() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent blocker = addCreatureReady(player2, new DuctCrawler());
        addAbilityMana(player1, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getCantBlockIds()).contains(crawler.getId());

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));

        assertThat(blocker.getCantBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("Can activate ability targeting own creature")
    void canTargetOwnCreature() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent ownCreature = addCreatureReady(player1, new DuctCrawler());
        addAbilityMana(player1, 1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCantBlockIds()).contains(crawler.getId());
    }

    private void addAbilityMana(Player player, int activations) {
        harness.addMana(player, ManaColor.RED, activations);
        harness.addMana(player, ManaColor.COLORLESS, activations);
    }

    private Permanent addReadyCrawler(Player player) {
        DuctCrawler card = new DuctCrawler();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
