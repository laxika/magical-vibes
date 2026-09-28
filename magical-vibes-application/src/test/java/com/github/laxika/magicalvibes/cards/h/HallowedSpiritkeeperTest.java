package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HallowedSpiritkeeper.class, GrizzlyBears.class, Shock.class})
class HallowedSpiritkeeperTest extends BaseCardTest {

    @Test
    void createsOneSpiritForEachCreatureCardInOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        harness.addToBattlefield(player1, new HallowedSpiritkeeper());

        killWithShock(player2, player1, "Hallowed Spiritkeeper");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(3);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.getCard().isToken()).isTrue();
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
            assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        });
    }

    @Test
    void doesNotCountNoncreatureCardsOrCardsInOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new HallowedSpiritkeeper());

        killWithShock(player2, player1, "Hallowed Spiritkeeper");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
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
}
