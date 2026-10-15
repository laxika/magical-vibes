package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.l.LifesparkSpellbomb;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VaultOfTheArchangel.class, DawntreaderElk.class, LifesparkSpellbomb.class})
class VaultOfTheArchangelTest extends BaseCardTest {

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
        Permanent bear1 = addCreatureReady(player1, new DawntreaderElk());
        Permanent bear2 = addCreatureReady(player1, new DawntreaderElk());
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
        Permanent ownBear = addCreatureReady(player1, new DawntreaderElk());
        Permanent opponentBear = addCreatureReady(player2, new DawntreaderElk());
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
        Permanent bear = addCreatureReady(player1, new DawntreaderElk());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(bear.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Keyword ability uses the stack and pays its full mana and tap costs")
    void keywordAbilityUsesStackAndPaysCosts() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new VaultOfTheArchangel());
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(creature.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(creature.hasKeyword(Keyword.LIFELINK)).isFalse();

        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(creature.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The grant includes creatures present at resolution, but not later arrivals")
    void affectedCreaturesAreDeterminedAtResolution() {
        harness.addToBattlefield(player1, new VaultOfTheArchangel());
        addAbilityMana(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        Permanent beforeResolution = addCreatureReady(player1, new DawntreaderElk());

        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new DawntreaderElk());

        assertThat(beforeResolution.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(beforeResolution.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(afterResolution.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(afterResolution.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("An animated Vault gains its own granted keywords")
    void animatedVaultGainsItsOwnKeywords() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new VaultOfTheArchangel());
        vault.setSummoningSick(false);
        harness.addToBattlefield(player1, new LifesparkSpellbomb());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, 0, null, vault.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vault)).isTrue();
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vault, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, vault, Keyword.LIFELINK)).isTrue();
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
