package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(UrzasFactory.class)
class UrzasFactoryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Urza's Factory produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent factory = addFactoryReady(player1);

        gs.tapPermanent(gd, player1, battlefieldIndex(factory));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying {7} and tapping Urza's Factory creates an Assembly-Worker token")
    void createsAssemblyWorkerToken() {
        Permanent factory = addFactoryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, battlefieldIndex(factory), 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Assembly-Worker");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ASSEMBLY_WORKER);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(factory.isTapped()).isTrue();
    }

    private Permanent addFactoryReady(Player player) {
        Permanent factory = new Permanent(new UrzasFactory());
        factory.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(factory);
        return factory;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
