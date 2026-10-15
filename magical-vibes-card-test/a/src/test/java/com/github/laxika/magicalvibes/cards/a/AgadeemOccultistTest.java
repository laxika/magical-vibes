package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WalkingAtlas;
import com.github.laxika.magicalvibes.cards.h.HalimarDepths;
import com.github.laxika.magicalvibes.cards.l.LoamLion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgadeemOccultist.class, LoamLion.class, WalkingAtlas.class, HalimarDepths.class})
class AgadeemOccultistTest extends BaseCardTest {

    @Test
    @DisplayName("Reanimates a creature whose mana value is within the Ally count")
    void reanimatesCreatureWithinAllyCount() {
        addOccultist();
        Card target = new LoamLion();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Counts all Allies you control when checking the mana value limit")
    void countsAlliesForManaValueLimit() {
        addOccultist();
        addAlly();
        Card target = new WalkingAtlas();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Can target a creature above the Ally count but does nothing on resolution")
    void canTargetCreatureAboveAllyCount() {
        addOccultist();
        Card target = new WalkingAtlas();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Walking Atlas");
        harness.assertNotOnBattlefield(player1, "Walking Atlas");
    }

    @Test
    @DisplayName("Rechecks the Ally count when the ability resolves")
    void rechecksAllyCountOnResolution() {
        addOccultist();
        Permanent extraAlly = addAlly();
        Card target = new WalkingAtlas();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(extraAlly);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void countsAlliesAddedAfterActivation() {
        addOccultist();
        Card target = new WalkingAtlas();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        addAlly();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Walking Atlas");
        harness.assertNotInGraveyard(player2, "Walking Atlas");
    }

    @Test
    void opponentsAlliesDoNotCount() {
        addOccultist();
        harness.addToBattlefield(player2, new AgadeemOccultist());
        Card target = new WalkingAtlas();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Walking Atlas");
        harness.assertNotOnBattlefield(player1, "Walking Atlas");
    }

    @Test
    void rejectsOwnGraveyard() {
        addOccultist();
        Card target = new LoamLion();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNoncreatureCard() {
        addOccultist();
        Card target = new HalimarDepths();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNothingWhenTargetLeavesGraveyard() {
        addOccultist();
        Card target = new LoamLion();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Loam Lion");
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new AgadeemOccultist());
        Card target = new LoamLion();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addOccultist() {
        Permanent occultist = harness.addToBattlefieldAndReturn(player1, new AgadeemOccultist());
        occultist.setSummoningSick(false);
        return occultist;
    }

    private Permanent addAlly() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new AgadeemOccultist());
        permanent.setSummoningSick(false);
        return permanent;
    }

}
