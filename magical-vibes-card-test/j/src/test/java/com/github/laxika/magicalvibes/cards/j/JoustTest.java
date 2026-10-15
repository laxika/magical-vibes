package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.w.WildbornPreserver;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Joust.class, YouthfulKnight.class, WildbornPreserver.class, Gingerbrute.class})
class JoustTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a Knight before it fights")
    void boostsKnightBeforeFight() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());

        castJoust(knight, opponent);

        assertThat(knight.getEffectivePower()).isEqualTo(4);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Youthful Knight");
        harness.assertInGraveyard(player2, "Gingerbrute");
    }

    @Test
    @DisplayName("Does not boost a non-Knight before it fights")
    void doesNotBoostNonKnight() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WildbornPreserver());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WildbornPreserver());

        castJoust(ownCreature, opponent);

        harness.assertInGraveyard(player1, "Wildborn Preserver");
        harness.assertInGraveyard(player2, "Wildborn Preserver");
    }

    @Test
    @DisplayName("The Knight's bonus lasts until end of turn")
    void bonusWearsOffAtEndOfTurn() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());

        castJoust(knight, opponent);

        assertThat(knight.getEffectivePower()).isEqualTo(4);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires a creature you control and a creature an opponent controls")
    void rejectsIllegalTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WildbornPreserver());
        Permanent otherOwnCreature = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WildbornPreserver());
        harness.setHand(player1, List.of(new Joust()));
        addJoustMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponent.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ownCreature.getId(), otherOwnCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("First strike does not prevent simultaneous fight damage")
    void firstStrikeDoesNotPreventFightDamage() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());

        castJoust(knight, opponent);

        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertInGraveyard(player2, "Youthful Knight");
    }

    @Test
    @DisplayName("A legal Knight still gets the bonus when the opposing target leaves")
    void boostsKnightWhenOpponentLeavesBeforeResolution() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());

        castJoustWithoutResolving(knight, opponent);
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(4);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
        assertThat(knight.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Youthful Knight");
        harness.assertInGraveyard(player1, "Joust");
    }

    @Test
    @DisplayName("The opposing creature takes no damage when the controlled target leaves")
    void doesNotFightWhenOwnCreatureLeavesBeforeResolution() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());

        castJoustWithoutResolving(knight, opponent);
        gd.playerBattlefields.get(player1.getId()).remove(knight);
        harness.passBothPriorities();

        assertThat(opponent.getMarkedDamage()).isZero();
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Youthful Knight");
        harness.assertInGraveyard(player1, "Joust");
    }

    @Test
    @DisplayName("An opposing target gained by the caster cannot fight but the Knight still gets its bonus")
    void doesNotFightWhenOpponentChangesController() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());

        castJoustWithoutResolving(knight, opponent);
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        gd.playerBattlefields.get(player1.getId()).add(opponent);
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(4);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
        assertThat(knight.getMarkedDamage()).isZero();
        assertThat(opponent.getMarkedDamage()).isZero();
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knight, opponent);
    }

    @Test
    @DisplayName("A Knight lost to the opponent cannot receive the bonus or fight")
    void doesNotBoostOrFightWhenOwnCreatureChangesController() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());

        castJoustWithoutResolving(knight, opponent);
        gd.playerBattlefields.get(player1.getId()).remove(knight);
        gd.playerBattlefields.get(player2.getId()).add(knight);
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(1);
        assertThat(knight.getMarkedDamage()).isZero();
        assertThat(opponent.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(knight, opponent);
    }

    private void castJoustWithoutResolving(Permanent ownCreature, Permanent opponent) {
        harness.setHand(player1, List.of(new Joust()));
        addJoustMana();
        harness.castSorcery(player1, 0, List.of(ownCreature.getId(), opponent.getId()));
    }

    private void castJoust(Permanent ownCreature, Permanent opponent) {
        harness.setHand(player1, List.of(new Joust()));
        addJoustMana();
        harness.castAndResolveSorcery(player1, 0, List.of(ownCreature.getId(), opponent.getId()));
    }

    private void addJoustMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
