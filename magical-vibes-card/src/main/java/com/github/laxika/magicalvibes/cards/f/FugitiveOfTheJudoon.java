package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTwoPermanentsThenSearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "103")
public class FugitiveOfTheJudoon extends Card {

    public FugitiveOfTheJudoon() {
        CreateTokenEffect humanToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Human", 1, 1, CardColor.WHITE, null,
                List.of(CardSubtype.HUMAN), Set.of(Keyword.WARD), Set.of(), false, false,
                Map.of(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2)),
                List.of(), false, false, false, 0, Set.of());
        CreateTokenEffect rhinoToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Alien Rhino", 4, 4, CardColor.WHITE, null,
                List.of(CardSubtype.ALIEN, CardSubtype.RHINO), Set.of(), Set.of(), false, false,
                Map.of(), List.of(), false, false, false, 0, Set.of());

        addEffect(EffectSlot.SAGA_CHAPTER_I, SequenceEffect.of(humanToken, rhinoToken));
        addEffect(EffectSlot.SAGA_CHAPTER_II, CreateTokenEffect.ofClueToken(1));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new MayEffect(
                new ExileTwoPermanentsThenSearchLibraryEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN), "Human",
                        new PermanentIsArtifactPredicate(), "artifact",
                        new CardSubtypePredicate(CardSubtype.DOCTOR)),
                "Exile a Human you control and an artifact you control?"));
    }
}
