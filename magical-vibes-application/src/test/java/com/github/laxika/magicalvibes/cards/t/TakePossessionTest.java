package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Imperiosaur;
import com.github.laxika.magicalvibes.cards.n.NimbusMaze;
import com.github.laxika.magicalvibes.cards.p.PatriciansScorn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TakePossession.class, Imperiosaur.class, NimbusMaze.class, PatriciansScorn.class})
class TakePossessionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Take Possession gives control of an opponent's creature")
    void resolvingStealsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Imperiosaur());
        harness.setHand(player1, List.of(new TakePossession()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Resolving Take Possession gives control of an opponent's land")
    void resolvingStealsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new NimbusMaze());
        harness.setHand(player1, List.of(new TakePossession()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(land.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Take Possession can enchant a permanent its controller already controls")
    void canEnchantOwnPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NimbusMaze());
        harness.setHand(player1, List.of(new TakePossession()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(land.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof TakePossession
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(land.getId()));
    }

    @Test
    @DisplayName("Destroying Take Possession returns the enchanted permanent to its owner")
    void permanentReturnsWhenAuraIsDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Imperiosaur());
        harness.setHand(player1, List.of(new TakePossession()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Take Possession");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        PatriciansScorn scorn = new PatriciansScorn();

        harness.passPriority(player1);
        harness.castFromHand(player2, scorn, "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
    }
}
