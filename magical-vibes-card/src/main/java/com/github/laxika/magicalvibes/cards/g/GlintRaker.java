package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "BRC", collectorNumber = "7")
@CardRegistration(set = "BRC", collectorNumber = "54")
public class GlintRaker extends Card {

    public GlintRaker() {
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new GreatestManaValueAmongControlled(new PermanentIsArtifactPredicate()),
                new Fixed(0), GrantScope.SELF));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                new LookAtTopCardsEffect(
                        new GreatestManaValueAmongControlled(new PermanentIsArtifactPredicate()),
                        new Fixed(1), new CardTypePredicate(CardType.ARTIFACT),
                        LookDestination.GRAVEYARD, true, LibrarySearchDestination.HAND, false,
                        false, null, null, false, 0, true),
                "Reveal that many cards from the top of your library?"));
    }
}
