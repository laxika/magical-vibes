package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.cards.v.VolcanoImp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathBomb.class, AncientSpider.class, VolcanoImp.class, ForsakenCity.class})
class DeathBombTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Destroys a nonblack creature and its controller loses 2 life")
    void destroysTargetAndControllerLosesTwoLife() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AncientSpider());

        harness.setHand(player1, List.of(new DeathBomb()));
        giveMana();

        harness.setLife(player2, 20);

        harness.castInstantWithSacrifice(player1, 0, victim.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ancient Spider");
        harness.assertInGraveyard(player2, "Ancient Spider");
        harness.assertInGraveyard(player1, "Ancient Spider");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot regenerate the destroyed creature")
    void cannotBeRegenerated() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        victim.setRegenerationShield(1);

        harness.setHand(player1, List.of(new DeathBomb()));
        giveMana();

        harness.castInstantWithSacrifice(player1, 0, victim.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ancient Spider");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new VolcanoImp());

        harness.setHand(player1, List.of(new DeathBomb()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, victim.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ForsakenCity());

        harness.setHand(player1, List.of(new DeathBomb()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, land.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
