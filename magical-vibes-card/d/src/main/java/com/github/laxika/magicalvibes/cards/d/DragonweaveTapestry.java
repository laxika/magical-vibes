package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ConjureSpellbookIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "29")
public class DragonweaveTapestry extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("TDM", "213"),
            new DraftFromSpellbookEffect.SpellbookCard("TDM", "232"),
            new DraftFromSpellbookEffect.SpellbookCard("TDM", "221"),
            new DraftFromSpellbookEffect.SpellbookCard("TDM", "178"),
            new DraftFromSpellbookEffect.SpellbookCard("TDM", "234"));

    public DragonweaveTapestry() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureSpellbookIntoLibraryEffect(SPELLBOOK, 2));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.DRAGON),
                                new CardSubtypePredicate(CardSubtype.OMEN))),
                        List.of(new DrawCardEffect())));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
