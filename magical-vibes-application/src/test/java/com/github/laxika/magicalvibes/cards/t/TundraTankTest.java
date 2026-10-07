package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TundraTank.class, OtterPenguin.class})
class TundraTankTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a creature you control indestructible until end of turn")
    void etbGrantsIndestructibleUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        harness.setHand(player1, List.of(new TundraTank()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbRejectsCreatureControlledByOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OtterPenguin());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        harness.setHand(player1, List.of(new TundraTank()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Crew 1 animates Tundra Tank and firebending adds red mana until end of combat")
    void crewAndFirebending() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new TundraTank());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        vehicle.setSummoningSick(false);
        crew.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Tundra Tank can enter without any creatures to target")
    void entersWithoutCreatures() {
        harness.setHand(player1, List.of(new TundraTank()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tundra Tank");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick creature can crew, and animation lasts only until end of turn")
    void summoningSickCreatureCanCrewAndAnimationExpires() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new TundraTank());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    @DisplayName("Crew cannot be paid with a tapped creature")
    void tappedCreatureCannotCrew() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new TundraTank());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
