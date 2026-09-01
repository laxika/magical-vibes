package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Shadowspear.class, GrizzlyBears.class, CarnageTyrant.class, DarksteelCitadel.class})
class ShadowspearTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1, trample, and lifelink")
    void equippedCreatureGetsBonuses() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent spear = addShadowspearReady(player1);
        spear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Equip attaches Shadowspear to a creature you control")
    void equipAttachesToControlledCreature() {
        Permanent spear = addShadowspearReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spear.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The activated ability removes hexproof and indestructible from opposing permanents")
    void removesKeywordsFromOpposingPermanents() {
        addShadowspearReady(player1);
        Permanent opponentHexproofPermanent = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        Permanent opponentIndestructiblePermanent = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        Permanent ownIndestructiblePermanent = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gqs.hasKeyword(gd, opponentHexproofPermanent, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentIndestructiblePermanent, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentHexproofPermanent, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentIndestructiblePermanent, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownIndestructiblePermanent, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The keyword removal expires at end of turn")
    void keywordRemovalExpiresAtEndOfTurn() {
        addShadowspearReady(player1);
        Permanent opponentHexproofPermanent = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        Permanent opponentIndestructiblePermanent = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, opponentHexproofPermanent, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentIndestructiblePermanent, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentHexproofPermanent, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentIndestructiblePermanent, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    private Permanent addShadowspearReady(Player player) {
        Permanent permanent = new Permanent(new Shadowspear());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
