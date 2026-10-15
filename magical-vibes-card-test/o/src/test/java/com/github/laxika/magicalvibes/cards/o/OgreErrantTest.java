package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreErrant.class, YouthfulKnight.class, Gingerbrute.class})
class OgreErrantTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger only targets another attacking Knight")
    void attackTriggerRestrictsTargets() {
        Permanent ogre = addCreatureReady(player1, new OgreErrant());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());
        Permanent nonKnight = addCreatureReady(player1, new Gingerbrute());

        declareAttackers(List.of(0, 1, 2));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(knight.getId())
                .doesNotContain(ogre.getId(), nonKnight.getId());
    }

    @Test
    @DisplayName("Attack trigger grants menace until end of turn")
    void attackTriggerGrantsMenace() {
        addCreatureReady(player1, new OgreErrant());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, knight.getId());
        harness.passBothPriorities();

        assertThat(knight.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("Attack trigger rejects an attacking non-Knight")
    void rejectsAttackingNonKnight() {
        addCreatureReady(player1, new OgreErrant());
        Permanent nonKnight = addCreatureReady(player1, new Gingerbrute());
        addCreatureReady(player1, new YouthfulKnight());

        declareAttackers(List.of(0, 1, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonKnight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Nonattacking Knights and opposing Knights are not legal targets")
    void excludesNonattackingKnights() {
        addCreatureReady(player1, new OgreErrant());
        Permanent attacker = addCreatureReady(player1, new YouthfulKnight());
        Permanent idleKnight = addCreatureReady(player1, new YouthfulKnight());
        Permanent opposingKnight = addCreatureReady(player2, new YouthfulKnight());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(attacker.getId())
                .doesNotContain(idleKnight.getId(), opposingKnight.getId());
    }

    @Test
    @DisplayName("Attacking alone does not grant menace to Ogre Errant")
    void attackingAloneHasNoLegalTarget() {
        Permanent ogre = addCreatureReady(player1, new OgreErrant());
        Permanent idleKnight = addCreatureReady(player1, new YouthfulKnight());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gqs.hasKeyword(gd, ogre, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, idleKnight, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Ogre Errant does not trigger when only another Knight attacks")
    void doesNotTriggerWithoutAttacking() {
        addCreatureReady(player1, new OgreErrant());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Granted menace lasts through the end step and expires during cleanup")
    void menaceExpiresAtEndOfTurn() {
        addCreatureReady(player1, new OgreErrant());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, knight.getId());
        resolveAllTriggers();

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.MENACE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.MENACE)).isFalse();
    }
}
