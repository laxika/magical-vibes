package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlemagesBracers.class, LlanowarElves.class, ProdigalPyromancer.class})
class BattlemagesBracersTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has haste")
    void equippedCreatureHasHaste() {
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent bracers = addReady(player1, new BattlemagesBracers());
        bracers.setAttachedTo(pyromancer.getId());

        assertThat(gqs.hasKeyword(gd, pyromancer, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature's non-mana ability may be copied for {1}")
    void mayPayToCopyEquippedCreatureAbility() {
        harness.setLife(player2, 20);
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent bracers = addReady(player1, new BattlemagesBracers());
        bracers.setAttachedTo(pyromancer.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(player1, pyromancer, player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Mana abilities of the equipped creature are not copied")
    void manaAbilityIsNotCopied() {
        Permanent elves = addReady(player1, new LlanowarElves());
        Permanent bracers = addReady(player1, new BattlemagesBracers());
        bracers.setAttachedTo(elves.getId());

        int elvesIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(elves);
        harness.tapPermanent(player1, elvesIndex);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.pendingMayAbilities).isEmpty();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip {2} attaches the Bracers to a creature you control")
    void equipAttachesToCreature() {
        Permanent creature = addReady(player1, new LlanowarElves());
        Permanent bracers = addReady(player1, new BattlemagesBracers());
        harness.addMana(player1, ManaColor.WHITE, 2);

        int bracersIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(bracers);
        harness.activateAbility(player1, bracersIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bracers.getAttachedTo()).isEqualTo(creature.getId());
    }

    private void activate(Player player, Permanent permanent, UUID targetId) {
        int index = harness.getGameData().playerBattlefields.get(player.getId()).indexOf(permanent);
        harness.activateAbility(player, index, null, targetId);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
