package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.cards.s.SliceInTwain;
import com.github.laxika.magicalvibes.cards.r.RevokeExistence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WurmcoilEngine.class, SliceInTwain.class, RevokeExistence.class})
class WurmcoilEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Wurmcoil Engine puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new WurmcoilEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Wurmcoil Engine");
    }

    @Test
    @DisplayName("When Wurmcoil Engine dies, two Phyrexian Wurm tokens are created")
    void deathTriggerCreatesTwoTokens() {
        harness.addToBattlefield(player1, new WurmcoilEngine());

        // Destroy Wurmcoil Engine with Slice in Twain
        harness.setHand(player1, List.of(new SliceInTwain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setLibrary(player1, List.of(new WurmcoilEngine()));
        harness.castInstant(player1, 0, findPermanent(player1, "Wurmcoil Engine").getId());
        harness.passBothPriorities(); // Resolve Slice in Twain — Wurmcoil Engine dies

        // Wurmcoil Engine should be in the graveyard
        harness.assertInGraveyard(player1, "Wurmcoil Engine");

        // Both tokens are created by a single triggered ability.
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Phyrexian Wurm")).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        // Two Phyrexian Wurm tokens should be on the battlefield
        List<Permanent> tokens = findPermanents(player1, "Phyrexian Wurm");
        assertThat(tokens).hasSize(2);
    }

    @Test
    @DisplayName("Death trigger tokens are 3/3 colorless Phyrexian Wurm artifact creatures")
    void tokensHaveCorrectProperties() {
        harness.addToBattlefield(player1, new WurmcoilEngine());

        harness.setHand(player1, List.of(new SliceInTwain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setLibrary(player1, List.of(new WurmcoilEngine()));
        harness.castInstant(player1, 0, findPermanent(player1, "Wurmcoil Engine").getId());
        harness.passBothPriorities(); // Resolve Slice in Twain
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Phyrexian Wurm");
        assertThat(tokens).hasSize(2);

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(3);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes())
                    .contains(CardSubtype.PHYREXIAN, CardSubtype.WURM);
            assertThat(token.getCard().isToken()).isTrue();
        }
    }

    @Test
    @DisplayName("One death token has deathtouch and the other has lifelink")
    void tokensHaveCorrectKeywords() {
        harness.addToBattlefield(player1, new WurmcoilEngine());

        harness.setHand(player1, List.of(new SliceInTwain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setLibrary(player1, List.of(new WurmcoilEngine()));
        harness.castInstant(player1, 0, findPermanent(player1, "Wurmcoil Engine").getId());
        harness.passBothPriorities(); // Resolve Slice in Twain
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Phyrexian Wurm");
        assertThat(tokens).hasSize(2);

        // One token should have deathtouch, the other lifelink
        assertThat(tokens).anyMatch(p -> p.getCard().getKeywords().contains(Keyword.DEATHTOUCH));
        assertThat(tokens).anyMatch(p -> p.getCard().getKeywords().contains(Keyword.LIFELINK));

        // Each token should have exactly one of the two keywords
        Permanent deathtouchToken = tokens.stream()
                .filter(p -> p.getCard().getKeywords().contains(Keyword.DEATHTOUCH))
                .findFirst().orElseThrow();
        assertThat(deathtouchToken.getCard().getKeywords()).doesNotContain(Keyword.LIFELINK);

        Permanent lifelinkToken = tokens.stream()
                .filter(p -> p.getCard().getKeywords().contains(Keyword.LIFELINK))
                .findFirst().orElseThrow();
        assertThat(lifelinkToken.getCard().getKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Exiling Wurmcoil Engine does not create death tokens")
    void exileDoesNotTrigger() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new WurmcoilEngine());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, engine.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(engine.getCard().getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Wurmcoil Engine");
    }

    @Test
    @DisplayName("Wurmcoil Engine gains life from unblocked combat damage")
    void unblockedCombatGainsLife() {
        addCreatureReady(player1, new WurmcoilEngine());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}
