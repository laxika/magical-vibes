package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCardTypeFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "116")
@CardRegistration(set = "WHO", collectorNumber = "721")
public class Blink extends Card {

    public Blink() {
        ShuffleTargetPermanentIntoLibraryEffect shuffleAndInvestigate =
                new ShuffleTargetPermanentIntoLibraryEffect(
                        CreateTokenEffect.ofClueToken(1), ThenEffectRecipient.TARGET_OWNER);
        addEffect(EffectSlot.SAGA_CHAPTER_I, shuffleAndInvestigate);
        addEffect(EffectSlot.SAGA_CHAPTER_III, shuffleAndInvestigate);
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(TargetFilters.creature()));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(TargetFilters.creature()));

        CreateTokenEffect alienAngel = new CreateTokenEffect(
                1,
                "Alien Angel",
                2,
                2,
                CardColor.BLACK,
                List.of(CardSubtype.ALIEN, CardSubtype.ANGEL),
                Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE),
                Set.of(CardType.ARTIFACT),
                Map.of(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                        new SpellCastTriggerEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                List.of(new RemoveCardTypeFromTargetPermanentEffect(
                                        CardType.CREATURE, GrantScope.SELF)))));
        addEffect(EffectSlot.SAGA_CHAPTER_II, alienAngel);
        addEffect(EffectSlot.SAGA_CHAPTER_IV, alienAngel);
    }
}
