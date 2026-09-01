package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurseOfTheWerefox.class, GrizzlyBears.class, LlanowarElves.class})
class CurseOfTheWerefoxTest extends BaseCardTest {

    @Test
    void createsMonsterRoleAndReflexivelyFightsChosenCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new LlanowarElves());
        castCurse(target);

        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void mayDeclineTheFightTarget() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new LlanowarElves());
        castCurse(target);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(findPermanent(player1, "Monster").getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void cannotTargetAnOpponentsCreatureForTheRole() {
        Permanent opponent = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new CurseOfTheWerefox()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsTheRoleWithoutAReflexiveFightWhenNoOpponentCreatureExists() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castCurse(target);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Monster").getAttachedTo()).isEqualTo(target.getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void castCurse(Permanent target) {
        harness.setHand(player1, List.of(new CurseOfTheWerefox()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, target.getId());
    }
}
