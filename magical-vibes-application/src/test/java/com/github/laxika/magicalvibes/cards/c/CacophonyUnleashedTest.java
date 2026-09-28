package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CacophonyUnleashed.class, GrizzlyBears.class, GloriousAnthem.class})
class CacophonyUnleashedTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, destroys nonenchantment creatures but preserves enchantments")
    void castDestroysNonenchantmentCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        harness.setHand(player1, List.of(new CacophonyUnleashed()));
        addCacophonyMana();
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantment);
        assertThat(findPermanent(player1, "Cacophony Unleashed")).isNotNull();
    }

    @Test
    @DisplayName("An enchantment entry animates Cacophony Unleashed until end of turn")
    void enchantmentEntryAnimatesSource() {
        Permanent cacophony = harness.addToBattlefieldAndReturn(player1, new CacophonyUnleashed());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.isEnchantment(gd, cacophony)).isTrue();
        assertThat(gqs.isCreature(gd, cacophony)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, cacophony))
                .containsExactlyInAnyOrder(CardSubtype.NIGHTMARE, CardSubtype.GOD);
        assertThat(gqs.getEffectivePower(gd, cacophony)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, cacophony)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, cacophony, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, cacophony, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, cacophony, CardSupertype.LEGENDARY)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cacophony)).isFalse();
        assertThat(gqs.hasEffectiveSupertype(gd, cacophony, CardSupertype.LEGENDARY)).isFalse();
    }

    private void addCacophonyMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
