package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarplusanStrider.class, Boomerang.class, GrizzlyBears.class, RoyalAssassin.class,
        Shock.class, Terror.class})
class KarplusanStriderTest extends BaseCardTest {

    

    @Test
    @DisplayName("Blue spells cannot target Karplusan Strider")
    void blueSpellsCannotTarget() {
        harness.addToBattlefield(player2, new KarplusanStrider());

        // Add valid target so spell is playable
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Karplusan Strider")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of blue spells");
    }

    @Test
    @DisplayName("Blue spells controlled by Karplusan Strider's controller cannot target it")
    void ownBlueSpellsCannotTarget() {
        harness.addToBattlefield(player1, new KarplusanStrider());

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Karplusan Strider")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of blue spells");
    }

    @Test
    @DisplayName("Black spells cannot target Karplusan Strider")
    void blackSpellsCannotTarget() {
        harness.addToBattlefield(player2, new KarplusanStrider());

        // Add valid target so spell is playable
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Karplusan Strider")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of black spells");
    }

    @Test
    @DisplayName("Non-blue and non-black spells can target Karplusan Strider")
    void otherColoredSpellsCanTarget() {
        harness.addToBattlefield(player2, new KarplusanStrider());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Karplusan Strider"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("Black activated abilities can still target Karplusan Strider")
    void blackActivatedAbilitiesCanTarget() {
        Permanent strider = addCreatureReady(player1, new KarplusanStrider());
        strider.tap();

        addCreatureReady(player2, new RoyalAssassin());

        harness.activateAbility(player2, 0, null, strider.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Karplusan Strider");
        harness.assertInGraveyard(player1, "Karplusan Strider");
    }
}
