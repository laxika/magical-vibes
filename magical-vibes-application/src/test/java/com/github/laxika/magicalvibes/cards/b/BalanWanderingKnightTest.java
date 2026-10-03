package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LightningGreaves;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalanWanderingKnight.class, BoneSaw.class, Bonesplitter.class,
        LightningGreaves.class, BludgeonBrawl.class, SolRing.class})
class BalanWanderingKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Balan has double strike only while two or more Equipment are attached")
    void doubleStrikeRequiresTwoAttachedEquipment() {
        Permanent balan = addCreatureReady(player1, new BalanWanderingKnight());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new BoneSaw());
        saw.setAttachedTo(balan.getId());

        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isFalse();

        Permanent splitter = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        splitter.setAttachedTo(balan.getId());
        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isTrue();

        splitter.setAttachedTo(null);
        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Balan attaches all Equipment controlled by the ability's controller")
    void attachesAllControlledEquipment() {
        Permanent balan = addCreatureReady(player1, new BalanWanderingKnight());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new BoneSaw());
        Permanent splitter = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent opponentSaw = harness.addToBattlefieldAndReturn(player2, new BoneSaw());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(saw.getAttachedTo()).isEqualTo(balan.getId());
        assertThat(splitter.getAttachedTo()).isEqualTo(balan.getId());
        assertThat(opponentSaw.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equipment controlled by an opponent still counts toward double strike")
    void opponentControlledEquipmentCountsTowardDoubleStrike() {
        Permanent balan = addCreatureReady(player1, new BalanWanderingKnight());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new BoneSaw());
        Permanent opponentSaw = harness.addToBattlefieldAndReturn(player2, new BoneSaw());
        saw.setAttachedTo(balan.getId());
        opponentSaw.setAttachedTo(balan.getId());

        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Balan can move Equipment from another creature despite having shroud")
    void movesEquipmentToShroudedBalan() {
        Permanent balan = harness.addToBattlefieldAndReturn(player1, new BalanWanderingKnight());
        Permanent greaves = harness.addToBattlefieldAndReturn(player1, new LightningGreaves());
        greaves.setAttachedTo(balan.getId());
        Permanent otherCreature = addCreatureReady(player2, new BalanWanderingKnight());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new BoneSaw());
        saw.setAttachedTo(otherCreature.getId());
        balan.setTapped(true);

        assertThat(gqs.hasKeyword(gd, balan, Keyword.SHROUD)).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(saw.getAttachedTo()).isEqualTo(balan.getId());
        assertThat(greaves.getAttachedTo()).isEqualTo(balan.getId());
        assertThat(balan.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Balan's ability resolves harmlessly when no Equipment is controlled")
    void resolvesWithoutEquipment() {
        Permanent balan = harness.addToBattlefieldAndReturn(player1, new BalanWanderingKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Balan attaches artifacts that became Equipment through Bludgeon Brawl")
    void attachesEquipmentGrantedByContinuousEffect() {
        Permanent balan = addCreatureReady(player1, new BalanWanderingKnight());
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ring.getAttachedTo()).isEqualTo(balan.getId());
    }

    @Test
    @DisplayName("Equipment granted by Bludgeon Brawl counts toward Balan's double strike")
    void countsEquipmentGrantedByContinuousEffect() {
        Permanent balan = addCreatureReady(player1, new BalanWanderingKnight());
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new BoneSaw());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        saw.setAttachedTo(balan.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 3, 1, null, balan.getId());
        harness.passBothPriorities();

        assertThat(ring.getAttachedTo()).isEqualTo(balan.getId());
        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
