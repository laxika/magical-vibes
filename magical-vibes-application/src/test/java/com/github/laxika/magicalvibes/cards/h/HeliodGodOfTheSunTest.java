package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeliodGodOfTheSunTest extends BaseCardTest {

    @Test
    @DisplayName("Heliod is not a creature below five devotion to white")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent heliod = addHeliod();
        addWhitePermanents(3);

        assertThat(gqs.isCreature(gd, heliod)).isFalse();
        assertThat(gqs.isEnchantment(gd, heliod)).isTrue();
    }

    @Test
    @DisplayName("Heliod becomes a creature at five devotion to white")
    void becomesCreatureAtDevotionThreshold() {
        Permanent heliod = addHeliod();
        addWhitePermanents(4);

        assertThat(gqs.isCreature(gd, heliod)).isTrue();
    }

    @Test
    @DisplayName("Other creatures you control have vigilance")
    void grantsVigilanceToOtherCreatures() {
        Permanent heliod = addHeliod();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, heliod, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Heliod creates a 2/1 white Cleric enchantment creature token")
    void createsClericEnchantmentCreatureToken() {
        Permanent heliod = addHeliod();
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(heliod), 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Cleric");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
    }

    private Permanent addHeliod() {
        return harness.addToBattlefieldAndReturn(player1, new HeliodGodOfTheSun());
    }

    private void addWhitePermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new SuntailHawk());
        }
    }
}
