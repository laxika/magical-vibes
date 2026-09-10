package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.s.StalkingStones;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColdStorage.class, MoggFanatic.class, StalkingStones.class})
class ColdStorageTest extends BaseCardTest {

    @Test
    @DisplayName("{3}: Exile target creature you control, tracked with Cold Storage")
    void exileAbilityExilesOwnCreature() {
        Permanent storage = harness.addToBattlefieldAndReturn(player1, new ColdStorage());
        harness.addToBattlefield(player1, new MoggFanatic());
        Permanent fanatic = findPermanent(player1, "Mogg Fanatic");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, fanatic.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mogg Fanatic");
        assertThat(gd.getCardsExiledByPermanent(storage.getId()))
                .anyMatch(c -> c.getName().equals("Mogg Fanatic"));
    }

    @Test
    @DisplayName("{3} cannot target a creature you don't control")
    void exileAbilityCannotTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new ColdStorage());
        harness.addToBattlefield(player2, new MoggFanatic());
        Permanent enemyFanatic = findPermanent(player2, "Mogg Fanatic");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enemyFanatic.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice: return each creature exiled with Cold Storage under your control")
    void sacrificeReturnsExiledCreatures() {
        Permanent storage = harness.addToBattlefieldAndReturn(player1, new ColdStorage());
        harness.addToBattlefield(player1, new MoggFanatic());
        Permanent fanatic = findPermanent(player1, "Mogg Fanatic");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, fanatic.getId());
        harness.passBothPriorities();
        assertThat(gd.getCardsExiledByPermanent(storage.getId())).hasSize(1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cold Storage");
        harness.assertInGraveyard(player1, "Cold Storage");
        harness.assertOnBattlefield(player1, "Mogg Fanatic");
        assertThat(gd.getCardsExiledByPermanent(storage.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice returns every creature exiled with Cold Storage")
    void sacrificeReturnsEveryExiledCreature() {
        Permanent storage = harness.addToBattlefieldAndReturn(player1, new ColdStorage());
        harness.addToBattlefield(player1, new MoggFanatic());
        harness.addToBattlefield(player1, new MoggFanatic());
        List<Permanent> fanatics = findPermanents(player1, "Mogg Fanatic");
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, fanatics.get(0).getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, fanatics.get(1).getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(storage.getId())).hasSize(2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mogg Fanatic")).isEqualTo(2);
        assertThat(gd.getCardsExiledByPermanent(storage.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice does not return a noncreature card that was animated when exiled")
    void sacrificeDoesNotReturnNoncreatureCardAnimatedWhenExiled() {
        harness.addToBattlefield(player1, new ColdStorage());
        Permanent stones = harness.addToBattlefieldAndReturn(player1, new StalkingStones());
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, stones.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stalking Stones");
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getName().equals("Stalking Stones"));
    }

    @Test
    @DisplayName("Sacrifice with no exiled cards still sacrifices the artifact")
    void sacrificeWithNoExiledCards() {
        harness.addToBattlefieldAndReturn(player1, new ColdStorage());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cold Storage");
        harness.assertInGraveyard(player1, "Cold Storage");
    }
}
