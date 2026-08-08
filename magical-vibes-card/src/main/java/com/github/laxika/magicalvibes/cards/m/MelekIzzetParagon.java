package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "84")
public class MelekIzzetParagon extends Card {

    public MelekIzzetParagon() {
        addEffect(EffectSlot.STATIC, new PlayWithTopCardRevealedEffect());
        addEffect(EffectSlot.STATIC,
                new AllowCastFromTopOfLibraryEffect(Set.of(CardType.INSTANT, CardType.SORCERY)));

        // The copy is mandatory (no cost, no MayEffect wrapper); only the "new targets for the copy"
        // choice is optional, and CopyControllerCastSpellEffect already offers it.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CopyControllerCastSpellOnSpellCastEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                Zone.LIBRARY));
    }
}
