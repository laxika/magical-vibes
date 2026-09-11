package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrengthOfSolidarity.class, BoggartBrute.class, FaerieMiscreant.class, FountainOfYouth.class,
        FugitiveWizard.class, GrizzlyBears.class, SoulWarden.class})
class StrengthOfSolidarityTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on a target creature equal to the size of your party")
    void putsCountersEqualToPartySize() {
        addFullParty();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castStrengthOfSolidarity(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature with two party types fills only one role")
    void oneCreatureCannotFillTwoPartyRoles() {
        Card multiRoleCard = new Card();
        multiRoleCard.setName("Cleric Rogue");
        multiRoleCard.setType(CardType.CREATURE);
        multiRoleCard.setManaCost("{2}");
        multiRoleCard.setPower(2);
        multiRoleCard.setToughness(2);
        multiRoleCard.setSubtypes(List.of(CardSubtype.CLERIC, CardSubtype.ROGUE));
        Permanent multiRole = new Permanent(multiRoleCard);
        gd.playerBattlefields.get(player1.getId()).add(multiRole);
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castStrengthOfSolidarity(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target only a creature you control")
    void cannotTargetOpponentCreatureOrNoncreaturePermanent() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StrengthOfSolidarity()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new StrengthOfSolidarity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void castStrengthOfSolidarity(Permanent target) {
        harness.setHand(player1, List.of(new StrengthOfSolidarity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
