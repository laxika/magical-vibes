package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetedBySpellColorsEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KarplusanStriderTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Karplusan Strider has correct card properties")
    void hasCorrectProperties() {
        KarplusanStrider card = new KarplusanStrider();

        assertThat(card.getName()).isEqualTo("Karplusan Strider");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getCardText()).isEqualTo("This creature can't be the target of blue or black spells.");
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(4);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isEqualTo(new CantBeTargetedBySpellColorsEffect(Set.of(CardColor.BLUE, CardColor.BLACK)));
    }

    @Test
    @DisplayName("Blue spells cannot target Karplusan Strider")
    void blueSpellsCannotTarget() {
        harness.addToBattlefield(player2, new KarplusanStrider());

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Karplusan Strider")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of blue spells");
    }

    @Test
    @DisplayName("Black spells cannot target Karplusan Strider")
    void blackSpellsCannotTarget() {
        harness.addToBattlefield(player2, new KarplusanStrider());

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
        Permanent strider = new Permanent(new KarplusanStrider());
        strider.setSummoningSick(false);
        strider.tap();
        harness.getGameData().playerBattlefields.get(player1.getId()).add(strider);

        Permanent assassin = new Permanent(new RoyalAssassin());
        assassin.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(assassin);

        harness.activateAbility(player2, 0, null, strider.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Karplusan Strider"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Karplusan Strider"));
    }
}
