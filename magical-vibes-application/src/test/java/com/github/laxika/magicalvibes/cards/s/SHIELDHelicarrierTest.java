package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SHIELDHelicarrier.class, AvatarOfMight.class})
class SHIELDHelicarrierTest extends BaseCardTest {

    @Test
    @DisplayName("S.H.I.E.L.D. Helicarrier creates two 1/1 white Soldier tokens when it enters")
    void createsTwoSoldiersWhenItEnters() {
        harness.setHand(player1, List.of(new SHIELDHelicarrier()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allMatch(soldier -> soldier.getCard().isToken()
                && soldier.getCard().getPower() == 1
                && soldier.getCard().getToughness() == 1
                && soldier.getCard().getColor() == CardColor.WHITE
                && soldier.getCard().getSubtypes().contains(CardSubtype.SOLDIER));
    }

    @Test
    @DisplayName("Crew 6 animates S.H.I.E.L.D. Helicarrier and taps the crew")
    void crewsHelicarrier() {
        Permanent helicarrier = addHelicarrierReady(player1);
        Permanent crew = addCreatureReady(player1, new AvatarOfMight());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(helicarrier.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, helicarrier)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crew animation ends at the end of the turn")
    void crewAnimationResetsAtEndOfTurn() {
        Permanent helicarrier = addHelicarrierReady(player1);
        addCreatureReady(player1, new AvatarOfMight());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(helicarrier.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, helicarrier)).isFalse();
    }

    private Permanent addHelicarrierReady(Player player) {
        Permanent permanent = new Permanent(new SHIELDHelicarrier());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player, AvatarOfMight creature) {
        Permanent permanent = new Permanent(creature);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
