package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExpeditionDiviner;
import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.g.GnarlidColony;
import com.github.laxika.magicalvibes.cards.z.ZulaportDuelist;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquadCommander.class, ZulaportDuelist.class, ExpeditionDiviner.class,
        ExpeditionHealer.class, GnarlidColony.class})
class SquadCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Kor Warrior token for each creature in your party")
    void entersWithTokensForPartySize() {
        addThreePartyRoles();

        harness.castFromHand(player1, new SquadCommander(), "{3}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> tokens = korWarriorTokens(player1);
        assertThat(tokens).hasSize(4);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.KOR, CardSubtype.WARRIOR);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("A full party gives your creatures +1/+0 and indestructible at beginning of combat")
    void fullPartyBoostsOwnCreatures() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new SquadCommander());
        addThreePartyRoles();
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GnarlidColony());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GnarlidColony());

        advanceToCombat(player1);

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not boost creatures without a full party")
    void doesNotBoostWithoutFullParty() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new SquadCommander());
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ZulaportDuelist());

        advanceToCombat(player1);

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Commander alone counts itself once, regardless of opposing party members")
    void commanderAloneCreatesOneToken() {
        harness.addToBattlefield(player2, new ExpeditionHealer());
        harness.addToBattlefield(player2, new ZulaportDuelist());
        harness.addToBattlefield(player2, new ExpeditionDiviner());

        harness.castFromHand(player1, new SquadCommander(), "{3}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(korWarriorTokens(player1)).hasSize(1);
        assertThat(korWarriorTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("Duplicate party roles do not increase the number of tokens")
    void duplicateRolesCountOnlyOnce() {
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new GnarlidColony());

        harness.castFromHand(player1, new SquadCommander(), "{3}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(korWarriorTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Entry trigger counts the party at resolution even if Commander has left")
    void entryTriggerUsesCurrentPartyWithoutCommander() {
        addThreePartyRoles();
        harness.castFromHand(player1, new SquadCommander(), "{3}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.getPermanentRemovalService().removePermanentToExile(gd,
                findPermanent(player1, "Squad Commander"));
        resolveAllTriggers();

        assertThat(korWarriorTokens(player1)).hasSize(3);
    }

    @Test
    @DisplayName("Losing a party member before combat trigger resolves prevents both bonuses")
    void fullPartyIsCheckedAgainAtResolution() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new SquadCommander());
        addThreePartyRoles();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);

        harness.getPermanentRemovalService().removePermanentToExile(gd,
                findPermanent(player1, "Expedition Diviner"));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A party completed after combat begins does not retroactively trigger")
    void completingPartyAfterCombatBeginsDoesNotTrigger() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new SquadCommander());
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ZulaportDuelist());
        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();

        harness.addToBattlefield(player1, new ExpeditionDiviner());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Combat bonuses persist after the party breaks and exclude later creatures")
    void resolvedBonusesPersistAndOnlyAffectExistingCreatures() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new SquadCommander());
        addThreePartyRoles();
        advanceToCombat(player1);

        harness.getPermanentRemovalService().removePermanentToExile(gd,
                findPermanent(player1, "Expedition Diviner"));
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new GnarlidColony());

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A full party does not grant bonuses during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new SquadCommander());
        addThreePartyRoles();

        advanceToCombat(player2);

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void addThreePartyRoles() {
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ZulaportDuelist());
        harness.addToBattlefield(player1, new ExpeditionDiviner());
    }

    private List<Permanent> korWarriorTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.KOR))
                .toList();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }
}
