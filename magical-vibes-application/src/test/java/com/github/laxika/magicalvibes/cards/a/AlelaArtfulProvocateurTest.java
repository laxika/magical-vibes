package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        AlelaArtfulProvocateur.class,
        Bitterblossom.class,
        CloudSprite.class,
        GrizzlyBears.class,
        Spellbook.class
})
class AlelaArtfulProvocateurTest extends BaseCardTest {

    @Test
    void boostsOwnFlyingCreaturesOnly() {
        AlelaArtfulProvocateur alela = new AlelaArtfulProvocateur();
        alela.setPower(10);
        alela.setToughness(10);
        harness.addToBattlefield(player1, alela);
        harness.addToBattlefield(player1, new CloudSprite());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CloudSprite());

        Permanent source = findPermanent(player1, "Alela, Artful Provocateur");
        Permanent ownFlying = findPermanent(player1, "Cloud Sprite");
        Permanent ownNonFlying = findPermanent(player1, "Grizzly Bears");
        Permanent opposingFlying = findPermanent(player2, "Cloud Sprite");

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, ownFlying)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownNonFlying)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingFlying)).isEqualTo(1);
    }

    @Test
    void artifactSpellCreatesFlyingFaerieToken() {
        harness.addToBattlefield(player1, new AlelaArtfulProvocateur());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Faerie");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    void enchantmentSpellCreatesTokenAndCreatureSpellDoesNot() {
        harness.addToBattlefield(player1, new AlelaArtfulProvocateur());
        harness.setHand(player1, List.of(new Bitterblossom()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
    }
}
