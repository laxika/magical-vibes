package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shacklegeist.class, SpiritOfMalevolence.class, WalkingCorpse.class, ConcordiaPegasus.class})
class ShacklegeistTest extends BaseCardTest {

    @Test
    void tapsTwoSpiritsIncludingItselfAndTapsOpponentCreature() {
        Permanent shacklegeist = addReady(player1, new Shacklegeist());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfMalevolence());
        Permanent extraSpirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfMalevolence());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, shacklegeist.getId());
        harness.handlePermanentChosen(player1, spirit.getId());
        harness.passBothPriorities();

        assertThat(shacklegeist.isTapped()).isTrue();
        assertThat(spirit.isTapped()).isTrue();
        assertThat(extraSpirit.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotTargetCreatureYouControl() {
        addReady(player1, new Shacklegeist());
        harness.addToBattlefield(player1, new SpiritOfMalevolence());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockFlyingCreature() {
        Permanent shacklegeist = addReady(player2, new Shacklegeist());
        Permanent attacker = addReady(player1, new ConcordiaPegasus());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(shacklegeist.isBlocking()).isTrue();
    }

    @Test
    void cannotBlockGroundCreature() {
        addReady(player2, new Shacklegeist());
        Permanent attacker = addReady(player1, new WalkingCorpse());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    void summoningSickSpiritsCanPayTheCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Shacklegeist());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfMalevolence());
        harness.addToBattlefield(player1, new SpiritOfMalevolence());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        source.setSummoningSick(true);
        spirit.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, spirit.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(spirit.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tappedSourceCanActivateUsingTwoOtherSpirits() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Shacklegeist());
        source.tap();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SpiritOfMalevolence());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SpiritOfMalevolence());
        harness.addToBattlefield(player1, new SpiritOfMalevolence());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotPayWithTappedSpiritsNonSpiritsOrOpponentsSpirits() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Shacklegeist());
        Permanent tappedSpirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfMalevolence());
        tappedSpirit.tap();
        Permanent nonSpirit = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent opposingSpirit = harness.addToBattlefieldAndReturn(player2, new SpiritOfMalevolence());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opposingSpirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");

        assertThat(source.isTapped()).isFalse();
        assertThat(nonSpirit.isTapped()).isFalse();
        assertThat(opposingSpirit.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
