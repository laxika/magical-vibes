package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.ExoskeletalArmor;
import com.github.laxika.magicalvibes.cards.f.FaithsFetters;
import com.github.laxika.magicalvibes.cards.n.NantukoMonastery;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenomousVines.class, ExoskeletalArmor.class, SuntailHawk.class,
        FaithsFetters.class, NantukoMonastery.class})
class VenomousVinesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an enchanted permanent")
    void destroysEnchantedPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        attachAura(creature, new ExoskeletalArmor());

        castAt(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Destroys an enchanted noncreature permanent")
    void destroysEnchantedNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new NantukoMonastery());
        attachAura(land, new FaithsFetters());

        castAt(land);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nantuko Monastery");
        harness.assertInGraveyard(player2, "Nantuko Monastery");
    }

    @Test
    @DisplayName("Cannot target an unenchanted permanent")
    void cannotTargetUnenchantedPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new VenomousVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchanted permanent");
    }

    @Test
    @DisplayName("Fizzles if the target is no longer enchanted")
    void fizzlesIfTargetIsNoLongerEnchanted() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent aura = attachAura(creature, new ExoskeletalArmor());

        castAt(creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertNotInGraveyard(player2, "Suntail Hawk");
    }

    private Permanent attachAura(Permanent permanent, Card auraCard) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, auraCard);
        aura.setAttachedTo(permanent.getId());
        return aura;
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new VenomousVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, target.getId());
    }
}
