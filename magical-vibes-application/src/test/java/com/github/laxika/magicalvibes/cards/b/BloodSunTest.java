package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DunesOfTheDead;
import com.github.laxika.magicalvibes.cards.h.HostileDesert;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodSunTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it enters")
    void drawsACardWhenItEnters() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BloodSun()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lands keep mana abilities but lose non-mana abilities")
    void preservesManaAbilitiesOnly() {
        Permanent hostileDesert = harness.addToBattlefieldAndReturn(player2, new HostileDesert());
        hostileDesert.setSummoningSick(false);
        resolveBloodSun();

        var bonus = gqs.computeStaticBonus(gd, hostileDesert);
        assertThat(bonus.losesAllNonManaAbilities()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hostileDesert);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lands lose their triggered abilities")
    void removesLandTriggeredAbilities() {
        harness.addToBattlefield(player2, new DunesOfTheDead());
        resolveBloodSun();

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Dunes of the Dead"));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    private void resolveBloodSun() {
        harness.setHand(player1, List.of(new BloodSun()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
