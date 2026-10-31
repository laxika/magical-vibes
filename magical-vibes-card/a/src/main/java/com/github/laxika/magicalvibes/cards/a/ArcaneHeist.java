package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "OTC", collectorNumber = "13")
@CardRegistration(set = "OTC", collectorNumber = "49")
public class ArcaneHeist extends Card {

    public ArcaneHeist() {
        // Cast target instant or sorcery card from an opponent's graveyard without paying its mana cost.
        // If that spell would be put into their graveyard, exile it instead.
        addSpellEffect(new CastTargetInstantOrSorceryFromGraveyardEffect(
                GraveyardSearchScope.OPPONENT_GRAVEYARD, true, true));

        // Cipher
        addSpellEffect(new MayEffect(new CipherEncodeEffect(), "Encode this spell on a creature you control?"));
    }
}
