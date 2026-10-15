package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetCreatureCardOntoTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "17")
public class PamperedLoamfrill extends Card {

    public PamperedLoamfrill() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new ConjureDuplicateOfTargetCreatureCardOntoTopOfLibraryEffect()),
                "Renew {1}{G} ({1}{G}, Exile this card from your graveyard: Conjure a duplicate of another target "
                        + "creature card in your graveyard onto the top of your library. The duplicate perpetually gets "
                        + "+1/+1 and gains deathtouch. Activate only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
