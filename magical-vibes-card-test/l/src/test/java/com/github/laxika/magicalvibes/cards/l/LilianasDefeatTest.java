package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.cards.t.TormentOfScarabs;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LilianasDefeat.class, WalkingCorpse.class, GrizzlyBears.class,
        LilianaDeathsMajesty.class, NicolBolasGodPharaoh.class, TormentOfScarabs.class})
class LilianasDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a black creature; no life lost when it isn't a Liliana")
    void destroysBlackCreatureWithoutLifeLoss() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new LilianasDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID corpseId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castSorcery(player1, 0, corpseId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Destroying a Liliana planeswalker makes its controller lose 3 life")
    void destroyingLilianaCostsControllerThreeLife() {
        harness.setLife(player2, 20);

        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaDeathsMajesty());
        liliana.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player1, List.of(new LilianasDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, liliana.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Liliana, Death's Majesty");
        harness.assertInGraveyard(player2, "Liliana, Death's Majesty");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot target a non-black creature")
    void cannotTargetNonBlackCreature() {
        // A legal black target makes the spell castable; aiming at the green creature is rejected.
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LilianasDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
    }

    @Test
    @DisplayName("Destroys a multicolored black planeswalker without the Liliana life loss")
    void destroysNonLilianaBlackPlaneswalkerWithoutLifeLoss() {
        harness.setLife(player2, 20);
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        bolas.setCounterCount(CounterType.LOYALTY, 7);
        harness.setHand(player1, List.of(new LilianasDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, bolas.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nicol Bolas, God-Pharaoh");
        harness.assertInGraveyard(player2, "Nicol Bolas, God-Pharaoh");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a black permanent that is neither a creature nor a planeswalker")
    void cannotTargetBlackEnchantment() {
        harness.addToBattlefield(player2, new NicolBolasGodPharaoh());
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new TormentOfScarabs());
        curse.setAttachedTo(player1.getId());
        harness.setHand(player1, List.of(new LilianasDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, curse.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
    }

    @Test
    @DisplayName("An indestructible Liliana survives but her controller still loses 3 life")
    void indestructibleLilianaStillCausesLifeLoss() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaDeathsMajesty());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        liliana.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new LilianasDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, liliana.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Liliana, Death's Majesty");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Targeting your own Liliana destroys her and makes you lose 3 life")
    void canDestroyOwnLiliana() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent liliana = harness.addToBattlefieldAndReturn(player1, new LilianaDeathsMajesty());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new LilianasDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, liliana.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Liliana, Death's Majesty");
        harness.assertInGraveyard(player1, "Liliana, Death's Majesty");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No life is lost if the targeted Liliana leaves before resolution")
    void missingTargetDoesNotCauseLifeLoss() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaDeathsMajesty());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new LilianasDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, liliana.getId());
        gd.playerBattlefields.get(player2.getId()).remove(liliana);
        harness.setHand(player2, List.of(liliana.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Liliana, Death's Majesty");
        harness.assertInGraveyard(player1, "Liliana's Defeat");
    }
}
