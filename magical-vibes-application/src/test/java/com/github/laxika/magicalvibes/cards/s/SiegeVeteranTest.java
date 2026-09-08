package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SiegeVeteranTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private List<Permanent> soldierTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Soldier".equals(permanent.getCard().getName()))
                .toList();
    }

    private void createSoldierToken() {
        harness.addToBattlefield(player1, new SiegeVeteran());
        harness.addToBattlefield(player1, new EliteVanguard());

        killWithShock(player1, player1, "Elite Vanguard");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Beginning of combat puts a +1/+1 counter on a target creature I control")
    void beginningOfCombatPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new SiegeVeteran());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Beginning of combat cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new SiegeVeteran());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(opponentBears.getId());
    }

    @Test
    @DisplayName("A nontoken Soldier dying creates a colorless artifact Soldier token")
    void createsSoldierTokenWhenNontokenSoldierDies() {
        createSoldierToken();

        assertThat(soldierTokens(player1)).hasSize(1);
        Permanent token = soldierTokens(player1).getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("A nontoken non-Soldier dying does not create a token")
    void doesNotCreateTokenForNonSoldier() {
        harness.addToBattlefield(player1, new SiegeVeteran());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player1, player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(soldierTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("A Soldier token dying does not create another token")
    void doesNotCreateTokenForTokenSoldier() {
        createSoldierToken();

        killWithShock(player1, player1, "Soldier");

        assertThat(gd.stack).isEmpty();
        assertThat(soldierTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("An opponent's nontoken Soldier dying does not create a token")
    void doesNotCreateTokenForOpponentSoldier() {
        harness.addToBattlefield(player1, new SiegeVeteran());
        harness.addToBattlefield(player2, new EliteVanguard());

        killWithShock(player1, player2, "Elite Vanguard");

        assertThat(gd.stack).isEmpty();
        assertThat(soldierTokens(player1)).isEmpty();
    }
}
