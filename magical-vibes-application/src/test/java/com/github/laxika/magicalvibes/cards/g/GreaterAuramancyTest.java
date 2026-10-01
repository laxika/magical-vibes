package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.cards.s.ShieldOfTheOversoul;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreaterAuramancy.class, SafeholdElite.class, ShieldOfTheOversoul.class})
class GreaterAuramancyTest extends BaseCardTest {

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new ShieldOfTheOversoul());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Other enchantments you control have shroud")
    void otherEnchantmentsYouControlHaveShroud() {
        harness.addToBattlefield(player1, new GreaterAuramancy());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        Permanent aura = attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, aura, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creatures you control have shroud")
    void enchantedCreaturesYouControlHaveShroud() {
        harness.addToBattlefield(player1, new GreaterAuramancy());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Greater Auramancy does not grant shroud to itself")
    void doesNotGrantShroudToItself() {
        Permanent auramancy = harness.addToBattlefieldAndReturn(player1, new GreaterAuramancy());

        assertThat(gqs.hasKeyword(gd, auramancy, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("An unenchanted creature you control does not have shroud")
    void unenchantedCreatureHasNoShroud() {
        harness.addToBattlefield(player1, new GreaterAuramancy());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Enchantments and enchanted creatures you do not control are unaffected")
    void doesNotAffectPermanentsYouDoNotControl() {
        harness.addToBattlefield(player1, new GreaterAuramancy());
        Permanent enemyCreature = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        Permanent enemyAura = attachAura(player2, enemyCreature);

        assertThat(gqs.hasKeyword(gd, enemyCreature, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, enemyAura, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("A creature you control is protected even when an opponent controls its Aura")
    void enchantedCreatureYouControlWithOpponentsAuraHasShroud() {
        harness.addToBattlefield(player1, new GreaterAuramancy());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        attachAura(player2, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Your Aura has shroud even when it enchants an opponent's creature")
    void ownAuraAttachedToOpponentsCreatureHasShroud() {
        harness.addToBattlefield(player1, new GreaterAuramancy());
        Permanent enemyCreature = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        Permanent aura = attachAura(player1, enemyCreature);

        assertThat(gqs.hasKeyword(gd, aura, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, enemyCreature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud is lost once Greater Auramancy leaves the battlefield")
    void shroudLostWhenAuramancyRemoved() {
        Permanent auramancy = harness.addToBattlefieldAndReturn(player1, new GreaterAuramancy());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        Permanent aura = attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auramancy);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.SHROUD)).isFalse();
    }
}
