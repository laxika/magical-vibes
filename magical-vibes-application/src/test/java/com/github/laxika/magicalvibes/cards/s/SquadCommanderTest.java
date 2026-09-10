package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquadCommander.class, FaerieMiscreant.class, FugitiveWizard.class,
        GrizzlyBears.class, SoulWarden.class})
class SquadCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Kor Warrior token for each creature in your party")
    void entersWithTokensForPartySize() {
        addThreePartyRoles();

        harness.setHand(player1, List.of(new SquadCommander()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not boost creatures without a full party")
    void doesNotBoostWithoutFullParty() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new SquadCommander());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());

        advanceToCombat(player1);

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void addThreePartyRoles() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new FugitiveWizard());
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
