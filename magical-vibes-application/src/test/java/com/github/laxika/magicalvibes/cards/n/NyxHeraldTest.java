package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.i.IndomitableWill;
import com.github.laxika.magicalvibes.cards.p.PheresBandBrawler;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NyxHerald.class, PheresBandBrawler.class, IndomitableWill.class, NyxbornCourser.class})
class NyxHeraldTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Boosts and grants trample to an enchanted creature")
    void boostsEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PheresBandBrawler());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IndomitableWill());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boosts and grants trample to an enchantment creature")
    void boostsEnchantmentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an unenchanted non-enchantment creature")
    void rejectsIneligibleCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PheresBandBrawler());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefield(player1, new NyxHerald());
        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Temporary boost and trample wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Nyx Herald can target itself without an Aura")
    void canTargetItself() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new NyxHerald());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();

        assertThat(herald.getPowerModifier()).isEqualTo(1);
        assertThat(herald.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, herald, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an opponent's enchantment creature")
    void rejectsOpponentsEnchantmentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature even if your Aura enchants it")
    void rejectsOpponentsEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PheresBandBrawler());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IndomitableWill());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Cannot target a noncreature enchantment")
    void rejectsNoncreatureEnchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PheresBandBrawler());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IndomitableWill());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("An opponent's Aura makes your creature an eligible target")
    void acceptsCreatureEnchantedByOpponentsAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PheresBandBrawler());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new IndomitableWill());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ability does not resolve if a non-enchantment target loses its last Aura")
    void targetLosingItsLastAuraBecomesIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PheresBandBrawler());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IndomitableWill());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An enchantment creature remains eligible after losing its Aura")
    void enchantmentCreatureRemainsLegalWithoutAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IndomitableWill());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Triggered ability resolves after Nyx Herald leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new NyxHerald());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(herald);
        gd.playerGraveyards.get(player1.getId()).add(herald.getCard());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }
}
