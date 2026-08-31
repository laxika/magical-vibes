package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CutIn.class, GrizzlyBears.class, SerraAngel.class})
class CutInTest extends BaseCardTest {

    @Test
    void dealsDamageAndAttachesYoungHeroRoleToTheSameCreature() {
        SerraAngel targetCard = new SerraAngel();
        targetCard.setToughness(5);
        Permanent target = addCreatureReady(player1, targetCard);
        harness.setHand(player1, List.of(new CutIn()));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId(), target.getId()));
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Young Hero");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void roleTargetCanBeOmitted() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CutIn()));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Young Hero")).isEmpty();
    }

    @Test
    void youngHeroPutsCounterOnSmallCreatureWhenItAttacks() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent damageTarget = addCreatureReady(player2, new SerraAngel());
        harness.setHand(player1, List.of(new CutIn()));
        addMana();

        harness.castSorcery(player1, 0, List.of(damageTarget.getId(), target.getId()));
        harness.passBothPriorities();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
