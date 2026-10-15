package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThopterShop.class, Forest.class, GrizzlyBears.class, Ornithopter.class, Shock.class})
class ThopterShopTest extends BaseCardTest {

    @Test
    void createsFlyingArtifactCreatureToken() {
        harness.addToBattlefield(player1, new ThopterShop());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().isToken()).isTrue();
        assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(thopter.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(thopter.getEffectivePower()).isEqualTo(1);
        assertThat(thopter.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void drawsWhenAnArtifactCreatureYouControlDies() {
        harness.addToBattlefield(player1, new ThopterShop());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyWithShock(player1, harness.getPermanentId(player1, "Ornithopter"));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void triggersOnlyOncePerTurn() {
        harness.addToBattlefield(player1, new ThopterShop());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyWithShock(player1, harness.getPermanentId(player1, "Ornithopter"));
        destroyWithShock(player1, harness.getPermanentId(player1, "Ornithopter"));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void doesNotTriggerForNonArtifactOrOpponentCreatures() {
        harness.addToBattlefield(player1, new ThopterShop());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyWithShock(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        destroyWithShock(player1, harness.getPermanentId(player2, "Ornithopter"));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void destroyWithShock(com.github.laxika.magicalvibes.model.Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
