package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class InstantCategoryClassifierTest {

    @BeforeAll
    static void loadOracleData() {
        // Ensure Scryfall oracle data is loaded for real card constructors
        new GameTestHarness();
    }

    @Nested
    @DisplayName("classify (instant spells)")
    class ClassifyInstants {

        @Test
        @DisplayName("Classifies Lightning Bolt as REMOVAL")
        void classifiesLightningBoltAsRemoval() {
            assertThat(InstantCategoryClassifier.classify(new LightningBolt()))
                    .isEqualTo(InstantCategory.REMOVAL);
        }
    }

    @Nested
    @DisplayName("classifyFlashCreature")
    class ClassifyFlashCreature {

        @Test
        @DisplayName("Classifies vanilla flash creature as FLASH_CREATURE")
        void classifiesVanillaFlashCreatureAsFlashCreature() {
            // Benalish Knight is a 2/2 first strike with flash — no ETB effects
            assertThat(InstantCategoryClassifier.classifyFlashCreature(new BenalishKnight()))
                    .isEqualTo(InstantCategory.FLASH_CREATURE);
        }

        @Test
        @DisplayName("Classifies flash creature with deathtouch (no ETB) as FLASH_CREATURE")
        void classifiesFlashCreatureWithKeywordsAsFlashCreature() {
            // Ambush Viper is a 1/1 deathtouch with flash — no ETB effects
            assertThat(InstantCategoryClassifier.classifyFlashCreature(new AmbushViper()))
                    .isEqualTo(InstantCategory.FLASH_CREATURE);
        }

        @Test
        @DisplayName("Classifies flash creature with removal ETB as REMOVAL")
        void classifiesFlashCreatureWithRemovalEtbAsRemoval() {
            Card flashRemoval = new Card();
            flashRemoval.setType(CardType.CREATURE);
            flashRemoval.setKeywords(Set.of(Keyword.FLASH));
            flashRemoval.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                    new DestroyTargetPermanentEffect(false, null));

            assertThat(InstantCategoryClassifier.classifyFlashCreature(flashRemoval))
                    .isEqualTo(InstantCategory.REMOVAL);
        }

        @Test
        @DisplayName("Classifies flash creature with card draw ETB as CARD_ADVANTAGE")
        void classifiesFlashCreatureWithDrawEtbAsCardAdvantage() {
            Card flashDraw = new Card();
            flashDraw.setType(CardType.CREATURE);
            flashDraw.setKeywords(Set.of(Keyword.FLASH));
            flashDraw.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));

            assertThat(InstantCategoryClassifier.classifyFlashCreature(flashDraw))
                    .isEqualTo(InstantCategory.CARD_ADVANTAGE);
        }

        @Test
        @DisplayName("Returns OTHER for non-flash creature")
        void returnsOtherForNonFlashCreature() {
            // Grizzly Bears has no flash keyword
            assertThat(InstantCategoryClassifier.classifyFlashCreature(new GrizzlyBears()))
                    .isEqualTo(InstantCategory.OTHER);
        }
    }
}
