package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VaultOfTheArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Has two activated abilities")
    void hasTwoActivatedAbilities() {
        VaultOfTheArchangel card = new VaultOfTheArchangel();

        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    

    

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new VaultOfTheArchangel());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Keyword ability grants deathtouch and lifelink to each creature you control")
    void keywordAbilityGrantsDeathtouchAndLifelinkToOwnCreatures() {
        harness.addToBattlefield(player1, new VaultOfTheArchangel());
        Permanent bear1 = addReadyCreature(player1, new GrizzlyBears());
        Permanent bear2 = addReadyCreature(player1, new GrizzlyBears());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(bear1.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bear1.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(bear2.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bear2.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Keyword ability does not affect opponent's creatures")
    void keywordAbilityDoesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new VaultOfTheArchangel());
        Permanent ownBear = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentBear = addReadyCreature(player2, new GrizzlyBears());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ownBear.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(ownBear.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(opponentBear.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(opponentBear.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new VaultOfTheArchangel());
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
