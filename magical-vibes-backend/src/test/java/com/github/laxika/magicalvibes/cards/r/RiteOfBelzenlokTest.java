package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiteOfBelzenlokTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I creates two 0/1 black Cleric tokens")
    void chapterIHasCorrectEffects() {
        RiteOfBelzenlok card = new RiteOfBelzenlok();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect effect = (CreateTokenEffect) effects.getFirst();
        assertThat(effect.amount()).isEqualTo(2);
        assertThat(effect.tokenName()).isEqualTo("Cleric");
        assertThat(effect.power()).isZero();
        assertThat(effect.toughness()).isEqualTo(1);
        assertThat(effect.color()).isEqualTo(CardColor.BLACK);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.CLERIC);
        assertThat(effect.keywords()).isEmpty();
    }

    @Test
    @DisplayName("Chapter II has same token creation as chapter I")
    void chapterIIHasCorrectEffects() {
        RiteOfBelzenlok card = new RiteOfBelzenlok();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect effect = (CreateTokenEffect) effects.getFirst();
        assertThat(effect.amount()).isEqualTo(2);
        assertThat(effect.tokenName()).isEqualTo("Cleric");
        assertThat(effect.power()).isZero();
        assertThat(effect.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III creates a 6/6 black Demon with flying, trample, and upkeep sacrifice trigger")
    void chapterIIIHasCorrectEffects() {
        RiteOfBelzenlok card = new RiteOfBelzenlok();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect effect = (CreateTokenEffect) effects.getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.tokenName()).isEqualTo("Demon");
        assertThat(effect.power()).isEqualTo(6);
        assertThat(effect.toughness()).isEqualTo(6);
        assertThat(effect.color()).isEqualTo(CardColor.BLACK);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.DEMON);
        assertThat(effect.keywords()).containsExactlyInAnyOrder(Keyword.FLYING, Keyword.TRAMPLE);
        assertThat(effect.tokenEffects()).hasSize(1);
        assertThat(effect.tokenEffects()).containsKey(EffectSlot.UPKEEP_TRIGGERED);
        assertThat(effect.tokenEffects().get(EffectSlot.UPKEEP_TRIGGERED))
                .isInstanceOf(SacrificeOtherCreatureOrDamageEffect.class);
        assertThat(((SacrificeOtherCreatureOrDamageEffect) effect.tokenEffects().get(EffectSlot.UPKEEP_TRIGGERED)).damage())
                .isEqualTo(6);
    }

    // ===== ETB: first lore counter and chapter I triggers =====

    @Test
    @DisplayName("Casting Rite of Belzenlok adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.setHand(player1, List.of(new RiteOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment

        GameData gd = harness.getGameData();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rite of Belzenlok"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getLoreCounters()).isEqualTo(1);

        // Chapter I ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    @Test
    @DisplayName("Chapter I resolving creates two 0/1 black Cleric tokens")
    void chapterICreatesTwoClerics() {
        harness.setHand(player1, List.of(new RiteOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();

        List<Permanent> clerics = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cleric") && p.getCard().isToken())
                .toList();
        assertThat(clerics).hasSize(2);
        for (Permanent cleric : clerics) {
            assertThat(cleric.getCard().getPower()).isZero();
            assertThat(cleric.getCard().getToughness()).isEqualTo(1);
            assertThat(cleric.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(cleric.getCard().getSubtypes()).contains(CardSubtype.CLERIC);
        }
    }

    // ===== Chapter II =====

    @Test
    @DisplayName("Chapter II creates two more Cleric tokens")
    void chapterIICreatesTwoMoreClerics() {
        harness.addToBattlefield(player1, new RiteOfBelzenlok());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rite of Belzenlok"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter II"));

        harness.passBothPriorities(); // resolve chapter II

        gd = harness.getGameData();

        long clericCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cleric") && p.getCard().isToken())
                .count();
        assertThat(clericCount).isEqualTo(2);
    }

    // ===== Chapter III: Demon token creation =====

    @Test
    @DisplayName("Chapter III creates a 6/6 black Demon token with flying and trample")
    void chapterIIICreatesDemonToken() {
        harness.addToBattlefield(player1, new RiteOfBelzenlok());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rite of Belzenlok"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(3);

        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        Permanent demon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Demon") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(demon).isNotNull();
        assertThat(demon.getCard().getPower()).isEqualTo(6);
        assertThat(demon.getCard().getToughness()).isEqualTo(6);
        assertThat(demon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(demon.getCard().getSubtypes()).contains(CardSubtype.DEMON);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.TRAMPLE);
    }

    // ===== Demon token upkeep trigger =====

    @Test
    @DisplayName("Demon token deals 6 damage to controller when no other creatures are present")
    void demonDealsDamageWhenNoOtherCreatures() {
        // Create a Demon token directly to test its upkeep trigger
        Permanent demon = addDemonToken(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("Demon token sacrifices another creature instead of dealing damage")
    void demonSacrificesOtherCreature() {
        Permanent demon = addDemonToken(player1);
        addCreature(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Grizzly Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // No damage dealt
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        // Demon remains
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Demon") && p.getCard().isToken());
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new RiteOfBelzenlok());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rite of Belzenlok"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        boolean sagaOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Rite of Belzenlok"));
        assertThat(sagaOnBf).isFalse();

        boolean sagaInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Rite of Belzenlok"));
        assertThat(sagaInGraveyard).isTrue();
    }

    @Test
    @DisplayName("Saga is not sacrificed while chapter III ability is on the stack")
    void sagaNotSacrificedWhileChapterOnStack() {
        harness.addToBattlefield(player1, new RiteOfBelzenlok());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rite of Belzenlok"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → lore counter 3, chapter III triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(3);
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    // ===== Helpers =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addDemonToken(Player player) {
        Card tokenCard = new Card();
        tokenCard.setToken(true);
        tokenCard.setName("Demon");
        tokenCard.setPower(6);
        tokenCard.setToughness(6);
        tokenCard.setColor(CardColor.BLACK);
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setSubtypes(List.of(CardSubtype.DEMON));
        tokenCard.setKeywords(java.util.Set.of(Keyword.FLYING, Keyword.TRAMPLE));
        tokenCard.addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeOtherCreatureOrDamageEffect(6));

        Permanent perm = new Permanent(tokenCard);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
