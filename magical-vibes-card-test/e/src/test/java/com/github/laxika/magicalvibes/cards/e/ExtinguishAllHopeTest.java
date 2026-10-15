package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BalefulEidolon;
import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxFleeceRam;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ExtinguishAllHope.class, GrizzlyBears.class, BalefulEidolon.class, GloriousAnthem.class,
        GoldenHind.class, NyxFleeceRam.class, FontOfFertility.class})
class ExtinguishAllHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nonenchantment creatures and spares enchantments and enchantment creatures")
    void destroysNonenchantmentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BalefulEidolon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new ExtinguishAllHope()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Baleful Eidolon");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @CardUsed({ExtinguishAllHope.class, GoldenHind.class, NyxFleeceRam.class, FontOfFertility.class})
    @DisplayName("Destroyed creatures reach their owners' graveyards while both players' enchantment creatures survive")
    void destroysCreaturesOnBothBattlefieldsToGraveyards() {
        harness.addToBattlefield(player1, new GoldenHind());
        harness.addToBattlefield(player2, new GoldenHind());
        harness.addToBattlefield(player1, new NyxFleeceRam());
        harness.addToBattlefield(player2, new NyxFleeceRam());
        harness.addToBattlefield(player2, new FontOfFertility());
        harness.setHand(player1, List.of(new ExtinguishAllHope()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Golden Hind");
        harness.assertInGraveyard(player2, "Golden Hind");
        harness.assertNotOnBattlefield(player1, "Golden Hind");
        harness.assertNotOnBattlefield(player2, "Golden Hind");
        harness.assertOnBattlefield(player1, "Nyx-Fleece Ram");
        harness.assertOnBattlefield(player2, "Nyx-Fleece Ram");
        harness.assertOnBattlefield(player2, "Font of Fertility");
        harness.assertInGraveyard(player1, "Extinguish All Hope");
    }

    @Test
    @CardUsed({ExtinguishAllHope.class})
    @DisplayName("Resolves without targets or any creatures on the battlefield")
    void resolvesOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new ExtinguishAllHope()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Extinguish All Hope");
    }
}
